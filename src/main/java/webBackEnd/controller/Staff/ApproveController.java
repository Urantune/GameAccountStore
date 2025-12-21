package webBackEnd.controller.Staff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.controller.Customer.CustomUserDetails;
import webBackEnd.entity.*;
import webBackEnd.service.*;
import webBackEnd.successfullyDat.GetQuantity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/staffHome")
public class ApproveController {

    private static final Logger log = LoggerFactory.getLogger(ApproveController.class);

    @Autowired private OrdersService ordersService;
    @Autowired private GetQuantity getQuantity;
    @Autowired private CustomerService customerService;
    @Autowired private OrderDetailService orderDetailService;
    @Autowired private AdministratorService administratorService;
    @Autowired private RentAccountGameService rentAccountGameService;
    @Autowired private GameAccountService gameAccountService;
    @Autowired private TransactionService transactionService;
    @Autowired private GameOwnedService gameOwnedService;
    @Autowired private VoucherCustomerService voucherCustomerService;
    @Autowired private MailAsyncService mailAsyncService;

    @GetMapping("/approveList")
    public String approveList(Model model) {
        List<Orders> list = ordersService.findAllByStatus("WAIT");
        list.sort(Comparator.comparing(Orders::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())));

        Map<UUID, List<OrderDetail>> orderDetailsMap = new HashMap<>();
        Map<UUID, String> orderTypeMap = new HashMap<>();

        for (Orders o : list) {
            List<OrderDetail> ods = orderDetailService.findAllByOrderId(o.getId());
            orderDetailsMap.put(o.getId(), ods);
            orderTypeMap.put(o.getId(), buildOrderTypeLabel(ods));
        }

        model.addAttribute("orderList", list);
        model.addAttribute("orderDetailsMap", orderDetailsMap);
        model.addAttribute("orderTypeMap", orderTypeMap);
        model.addAttribute("getQuantity", getQuantity);

        return "staff/ApproveList";
    }

    private String buildOrderTypeLabel(List<OrderDetail> ods) {
        if (ods == null || ods.isEmpty()) return "N/A";
        int rentCount = 0;
        Integer maxMonths = null;

        for (OrderDetail od : ods) {
            Integer d = (od != null ? od.getDuration() : null);
            if (d != null && d > 0) {
                rentCount++;
                if (maxMonths == null || d > maxMonths) maxMonths = d;
            }
        }

        if (rentCount == 0) return "BUY (FOREVER)";
        if (rentCount == ods.size()) return "RENT (max " + (maxMonths == null ? 0 : maxMonths) + " month(s))";
        return "MIXED";
    }

    @GetMapping("/approve/{orderId}")
    public String viewOrderDetail(@PathVariable UUID orderId, Model model) {
        Orders order = ordersService.findById(orderId);
        List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);

        Map<OrderDetail, List<GameAccount>> candidatesMap = new LinkedHashMap<>();
        if (orderDetails != null) {
            for (OrderDetail od : orderDetails) {
                candidatesMap.put(od, findCandidatesForOrderDetail(od));
            }
        }

        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("candidatesMap", candidatesMap);

        return "staff/OrderDetail";
    }

    @PostMapping("/approve/decision")
    @Transactional
    public String decision(@RequestParam UUID orderId,
                           @RequestParam String decision,
                           @RequestParam Map<String, String> params,
                           @AuthenticationPrincipal CustomUserDetails user) {

        log.info("[A000] decision start orderId={} decision={} paramsSize={}", orderId, decision, params == null ? 0 : params.size());

        Orders order = ordersService.findById(orderId);
        if (order == null) {
            log.warn("[A001] order not found orderId={}", orderId);
            return "redirect:/staffHome/approveList";
        }

        log.info("[A002] order loaded id={} status={} createdDate={} totalPrice={}", order.getId(), order.getStatus(), order.getCreatedDate(), order.getTotalPrice());

        final UUID DEFAULT_STAFF_ID = UUID.fromString("88A7A905-CB27-431C-BFED-1D16BEA9B91C");
        Staff staff = null;
        try {
            staff = administratorService.getStaffByID(DEFAULT_STAFF_ID);
            log.info("[A003] staff loaded id={} username={} role={} status={}", staff.getId(), staff.getUsername(), staff.getRole(), staff.getStatus());
        } catch (Exception ex) {
            log.warn("[A004] staff load fail defaultStaffId={} ex={}", DEFAULT_STAFF_ID, ex.toString());
        }

        Customer customer = order.getCustomer();
        if (customer == null && user != null) {
            customer = customerService.findCustomerByUsername(user.getUsername());
            log.info("[A005] customer resolved by auth username={} found={}", user.getUsername(), customer != null);
        } else {
            log.info("[A006] customer from order present={}", customer != null);
        }

        if ("REJECT".equalsIgnoreCase(decision)) {
            log.info("[A010] REJECT flow begin orderId={}", order.getId());

            if (customer != null && order.getTotalPrice() != null) {
                if (customer.getBalance() == null) customer.setBalance(BigDecimal.ZERO);
                BigDecimal before = customer.getBalance();
                customer.setBalance(customer.getBalance().add(order.getTotalPrice()));
                customerService.save(customer);

                log.info("[A011] refund balance customerId={} before={} refund={} after={}", customer.getCustomerId(), before, order.getTotalPrice(), customer.getBalance());

                Transaction tx = new Transaction();
                tx.setCustomer(customer);
                tx.setAmount(order.getTotalPrice());
                tx.setDescription("REJECT_ORDER_" + order.getId());
                tx.setDateCreated(LocalDateTime.now());
                transactionService.save(tx);

                log.info("[A012] tx saved refund customerId={} amount={} desc={}", customer.getCustomerId(), order.getTotalPrice(), tx.getDescription());
            } else {
                log.info("[A013] refund skipped customerNull={} totalNull={}", customer == null, order.getTotalPrice() == null);
            }

            if (order.getVoucher() != null && customer != null) {
                try {
                    voucherCustomerService.rollbackUsed(customer, order.getVoucher());
                    log.info("[A014] voucher rollback ok customerId={} voucher={}", customer.getCustomerId(), order.getVoucher().getId());
                } catch (Exception ex) {
                    log.warn("[A015] voucher rollback fail customerId={} ex={}", customer.getCustomerId(), ex.toString());
                }
            } else {
                log.info("[A016] voucher rollback skipped voucherNull={} customerNull={}", order.getVoucher() == null, customer == null);
            }

            order.setStatus("REJECTED");
            if (staff != null) order.setStaff(staff);
            ordersService.save(order);

            log.info("[A017] order status updated REJECTED orderId={} staffSet={}", order.getId(), staff != null);

            if (customer != null && customer.getEmail() != null) {
                mailAsyncService.sendRejectEmail(customer.getEmail(), order.getId().toString(), order.getTotalPrice());
                log.info("[A018] reject mail queued email={} orderId={}", customer.getEmail(), order.getId());
            } else {
                log.info("[A019] reject mail skipped customerNull={} emailNull={}", customer == null, customer == null || customer.getEmail() == null);
            }

            log.info("[A020] REJECT flow done orderId={}", order.getId());
            return "redirect:/staffHome/approveList";
        }

        List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);
        if (orderDetails == null || orderDetails.isEmpty()) {
            log.warn("[A030] ACCEPT blocked: no orderDetails orderId={}", orderId);
            return "redirect:/staffHome/approve/" + orderId;
        }

        log.info("[A031] orderDetails loaded size={} orderId={}", orderDetails.size(), orderId);

        Map<UUID, UUID> selected = parseSelectedMap(params);
        log.info("[A032] selected parsed size={} keys={}", selected.size(), selected.keySet());

        selected = normalizeSelectionKeepValid(orderDetails, selected);
        log.info("[A033] selected after normalize size={} keys={}", selected.size(), selected.keySet());

        selected = fillMissingByAutoPick(orderDetails, selected);
        log.info("[A034] selected after fillMissing size={} keys={}", selected.size(), selected.keySet());

        boolean ok = validateSelectedMatchesOrderDetails(orderDetails, selected);
        log.info("[A035] validateSelectedMatchesOrderDetails ok={} selectedSize={} odSize={}", ok, selected.size(), orderDetails.size());
        if (!ok) {
            log.warn("[A036] ACCEPT blocked: validate fail orderId={}", orderId);
            return "redirect:/staffHome/approve/" + orderId;
        }

        String accountHtml = handleAcceptAndPersist(order, orderDetails, selected);

        order.setStatus("COMPLETED");
        if (staff != null) order.setStaff(staff);
        ordersService.save(order);

        log.info("[A040] order completed orderId={} staffSet={} customerId={}", order.getId(), staff != null, customer == null ? null : customer.getCustomerId());

        if (customer != null && customer.getEmail() != null) {
            mailAsyncService.sendAcceptEmail(customer.getEmail(), accountHtml);
            log.info("[A041] accept mail queued email={} orderId={}", customer.getEmail(), order.getId());
        } else {
            log.info("[A042] accept mail skipped customerNull={} emailNull={}", customer == null, customer == null || customer.getEmail() == null);
        }

        log.info("[A099] decision end orderId={} decision={}", orderId, decision);
        return "redirect:/staffHome/approveList";
    }

    private Map<UUID, UUID> parseSelectedMap(Map<String, String> params) {
        Map<UUID, UUID> out = new LinkedHashMap<>();
        if (params == null || params.isEmpty()) return out;

        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if (k == null || v == null) continue;
            if (!k.startsWith("selected[")) continue;
            if (!k.endsWith("]")) continue;

            String odIdStr = k.substring("selected[".length(), k.length() - 1).trim();
            String gaIdStr = v.trim();
            if (odIdStr.isEmpty() || gaIdStr.isEmpty()) continue;

            try {
                UUID odId = UUID.fromString(odIdStr);
                UUID gaId = UUID.fromString(gaIdStr);
                out.put(odId, gaId);
            } catch (Exception ex) {
                log.warn("[A060] parseSelectedMap invalid uuid key={} val={} ex={}", k, v, ex.toString());
            }
        }

        return out;
    }

    private Map<UUID, UUID> normalizeSelectionKeepValid(List<OrderDetail> orderDetails, Map<UUID, UUID> selected) {
        Map<UUID, UUID> out = new LinkedHashMap<>();
        if (orderDetails == null || orderDetails.isEmpty()) return out;
        if (selected == null || selected.isEmpty()) return out;

        Map<UUID, OrderDetail> odMap = new HashMap<>();
        for (OrderDetail od : orderDetails) {
            if (od != null && od.getId() != null) odMap.put(od.getId(), od);
        }

        Set<UUID> used = new HashSet<>();
        for (Map.Entry<UUID, UUID> e : selected.entrySet()) {
            UUID odId = e.getKey();
            UUID gaId = e.getValue();
            if (odId == null || gaId == null) continue;

            OrderDetail od = odMap.get(odId);
            if (od == null) {
                log.info("[A070] normalize drop: od not in list odId={} gaId={}", odId, gaId);
                continue;
            }

            if (!used.add(gaId)) {
                log.info("[A071] normalize drop: duplicate gaId={} for odId={}", gaId, odId);
                continue;
            }

            GameAccount ga = gameAccountService.getGameById(gaId);
            if (ga == null) {
                log.info("[A072] normalize drop: ga not found gaId={} odId={}", gaId, odId);
                continue;
            }

            if (ga.getStatus() == null || !ga.getStatus().equalsIgnoreCase("ACTIVE")) {
                log.info("[A073] normalize drop: ga not ACTIVE gaId={} status={} odId={}", gaId, ga.getStatus(), odId);
                continue;
            }

            if (!isMatch(od, ga)) {
                log.info("[A074] normalize drop: mismatch odId={} gaId={}", odId, gaId);
                continue;
            }

            out.put(odId, gaId);
            log.info("[A075] normalize keep odId={} gaId={}", odId, gaId);
        }

        return out;
    }

    private Map<UUID, UUID> fillMissingByAutoPick(List<OrderDetail> orderDetails, Map<UUID, UUID> selected) {
        Map<UUID, UUID> out = new LinkedHashMap<>();
        if (selected != null) out.putAll(selected);

        Set<UUID> used = new HashSet<>();
        for (UUID v : out.values()) if (v != null) used.add(v);

        for (OrderDetail od : orderDetails) {
            if (od == null || od.getId() == null) continue;
            if (out.containsKey(od.getId())) {
                log.info("[A080] fillMissing skip already chosen odId={} gaId={}", od.getId(), out.get(od.getId()));
                continue;
            }

            List<GameAccount> candidates = findCandidatesForOrderDetail(od);
            log.info("[A081] fillMissing candidates odId={} size={}", od.getId(), candidates == null ? 0 : candidates.size());

            GameAccount pick = null;
            if (candidates != null) {
                for (GameAccount ga : candidates) {
                    if (ga == null || ga.getGameAccountId() == null) continue;
                    if (used.contains(ga.getGameAccountId())) continue;
                    pick = ga;
                    break;
                }
            }

            if (pick == null) {
                log.warn("[A082] fillMissing failed no pick odId={}", od.getId());
                return out;
            }

            out.put(od.getId(), pick.getGameAccountId());
            used.add(pick.getGameAccountId());
            log.info("[A083] fillMissing picked odId={} gaId={}", od.getId(), pick.getGameAccountId());
        }

        return out;
    }

    private boolean validateSelectedMatchesOrderDetails(List<OrderDetail> orderDetails, Map<UUID, UUID> selected) {
        if (orderDetails == null || orderDetails.isEmpty()) {
            log.warn("[A090] validate fail: orderDetails empty");
            return false;
        }
        if (selected == null) {
            log.warn("[A091] validate fail: selected null");
            return false;
        }
        if (selected.size() != orderDetails.size()) {
            log.warn("[A092] validate fail: size mismatch selected={} od={}", selected.size(), orderDetails.size());
            return false;
        }

        Set<UUID> used = new HashSet<>();
        for (OrderDetail od : orderDetails) {
            if (od == null || od.getId() == null) {
                log.warn("[A093] validate fail: od null/id null");
                return false;
            }

            UUID gaId = selected.get(od.getId());
            if (gaId == null) {
                log.warn("[A094] validate fail: missing selection odId={}", od.getId());
                return false;
            }

            if (!used.add(gaId)) {
                log.warn("[A095] validate fail: duplicate gaId={} odId={}", gaId, od.getId());
                return false;
            }

            GameAccount ga = gameAccountService.getGameById(gaId);
            if (ga == null) {
                log.warn("[A096] validate fail: ga not found gaId={} odId={}", gaId, od.getId());
                return false;
            }

            if (ga.getStatus() == null || !ga.getStatus().equalsIgnoreCase("ACTIVE")) {
                log.warn("[A097] validate fail: ga not ACTIVE gaId={} status={} odId={}", gaId, ga.getStatus(), od.getId());
                return false;
            }

            if (!isMatch(od, ga)) {
                log.warn("[A098] validate fail: mismatch odId={} gaId={}", od.getId(), gaId);
                return false;
            }

            log.info("[A100] validate ok odId={} gaId={}", od.getId(), gaId);
        }

        return true;
    }

    private boolean isMatch(OrderDetail od, GameAccount ga) {
        if (od == null || ga == null) return false;
        if (od.getGame() == null || ga.getGame() == null) return false;
        if (!Objects.equals(od.getGame().getGameId(), ga.getGame().getGameId())) return false;

        if (od.getPrice() == null || ga.getPrice() == null) return false;
        if (ga.getPrice().compareTo(BigDecimal.valueOf(od.getPrice())) != 0) return false;

        if (ga.getVip() != od.getVip()) return false;
        if (ga.getLovel() != od.getLovel()) return false;
        if (ga.getSkin() != od.getSkin()) return false;

        String odRank = od.getRank();
        if (odRank != null && !odRank.isBlank()) {
            if (ga.getRank() == null) return false;
            if (!ga.getRank().equalsIgnoreCase(odRank.trim())) return false;
        }

        boolean odRent = (od.getDuration() != null && od.getDuration() > 0);
        boolean gaRent = isRentAccount(ga);

        return odRent == gaRent;
    }

    private boolean isRentAccount(GameAccount ga) {
        if (ga == null) return false;

        String classify = ga.getClassify();
        String status = ga.getStatus();
        String durationAcc = ga.getDuration();

        boolean byClassify = (classify != null && classify.trim().equalsIgnoreCase("RENT"));
        boolean byStatus = (status != null && status.trim().equalsIgnoreCase("RENT"));
        boolean byDuration = (durationAcc != null && !durationAcc.trim().isEmpty() && !durationAcc.trim().equalsIgnoreCase("0"));

        return byClassify || byStatus || byDuration;
    }

    private List<GameAccount> findCandidatesForOrderDetail(OrderDetail od) {
        if (od == null || od.getGame() == null || od.getPrice() == null) return List.of();

        List<GameAccount> all = gameAccountService.findGameAccountByGame(od.getGame());
        if (all == null || all.isEmpty()) return List.of();

        BigDecimal odPrice = BigDecimal.valueOf(od.getPrice().longValue());
        boolean odRent = od.getDuration() != null && od.getDuration() > 0;

        List<GameAccount> out = new ArrayList<>();
        for (GameAccount ga : all) {
            if (ga == null) continue;
            if (ga.getStatus() == null || !ga.getStatus().equalsIgnoreCase("ACTIVE")) continue;
            if (ga.getPrice() == null || ga.getPrice().compareTo(odPrice) != 0) continue;

            boolean gaRent = isRentAccount(ga);
            if (odRent != gaRent) continue;

            if (!isMatch(od, ga)) continue;
            out.add(ga);
        }

        out.sort(Comparator.comparing(GameAccount::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return out;
    }

    private String handleAcceptAndPersist(Orders order,
                                          List<OrderDetail> orderDetails,
                                          Map<UUID, UUID> selected) {

        log.info("[B000] handleAcceptAndPersist start orderId={} odSize={} selSize={}",
                order == null ? null : order.getId(),
                orderDetails == null ? 0 : orderDetails.size(),
                selected == null ? 0 : selected.size());

        StringBuilder html = new StringBuilder();
        Customer customer = order.getCustomer();

        LocalDateTime baseTime = (order != null && order.getCreatedDate() != null) ? order.getCreatedDate() : LocalDateTime.now();
        log.info("[B001] baseTime={}", baseTime);

        for (OrderDetail od : orderDetails) {
            UUID odId = od == null ? null : od.getId();
            UUID gaId = (odId == null || selected == null) ? null : selected.get(odId);

            log.info("[B010] process odId={} gaId={}", odId, gaId);

            if (odId == null || gaId == null) {
                log.warn("[B011] skip odIdNullOrGaNull odId={} gaId={}", odId, gaId);
                continue;
            }

            GameAccount ga = gameAccountService.getGameById(gaId);
            if (ga == null) {
                log.warn("[B012] skip gaNotFound gaId={} odId={}", gaId, odId);
                continue;
            }

            od.setGameAccount(ga);
            orderDetailService.save(od);
            log.info("[B013] od updated odId={} setGameAccount={}", odId, gaId);

            boolean odRent = od.getDuration() != null && od.getDuration() > 0;
            log.info("[B014] odRent={} duration={}", odRent, od.getDuration());

            if (!odRent) {
                gameOwnedService.createOwnedIfNotExists(customer, ga);
                ga.setStatus("SOLD");
                gameAccountService.save(ga);
                log.info("[B015] BUY persisted gaId={} status=SOLD customerId={}", gaId, customer == null ? null : customer.getCustomerId());
            } else {
                RentAccountGame rent = new RentAccountGame();
                rent.setCustomer(customer);
                rent.setGameAccount(ga);
                rent.setDateStart(baseTime);
                rent.setDateEnd(baseTime.plusMonths(od.getDuration()));
                rent.setStatus("STILL VALID");
                rentAccountGameService.save(rent);

                ga.setStatus("IN USE");
                gameAccountService.save(ga);

                log.info("[B016] RENT persisted rentId={} gaId={} status=IN USE dateStart={} dateEnd={} customerId={}",
                        rent.getId(), gaId, rent.getDateStart(), rent.getDateEnd(), customer == null ? null : customer.getCustomerId());
            }

            String gameName = ga.getGame() != null ? ga.getGame().getGameName() : "N/A";
            String acc = ga.getGameAccount() != null ? ga.getGameAccount() : "";
            String pass = ga.getGamePassword() != null ? ga.getGamePassword() : "";
            String price = ga.getPrice() != null ? ga.getPrice().toPlainString() : "0";

            html.append("<b>TRÒ CHƠI:</b> ").append(gameName).append("<br>")
                    .append("<b>TÀI KHOẢN:</b> ").append(acc).append("<br>")
                    .append("<b>MẬT KHẨU:</b> ").append(pass).append("<br>")
                    .append("<b>HÌNH THỨC:</b> ").append(odRent ? ("THUÊ " + od.getDuration() + " THÁNG") : "MUA VĨNH VIỄN")
                    .append("<br>")
                    .append("<b>GIÁ:</b> ").append(price).append(" đ<br><br>");

            log.info("[B017] html appended odId={} gaId={}", odId, gaId);
        }

        log.info("[B099] handleAcceptAndPersist end orderId={} htmlLen={}", order == null ? null : order.getId(), html.length());
        return html.toString();
    }
}
