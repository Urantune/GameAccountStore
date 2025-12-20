package webBackEnd.controller.Staff;

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

        Orders order = ordersService.findById(orderId);
        if (order == null) return "redirect:/staffHome/approveList";

        final UUID DEFAULT_STAFF_ID = UUID.fromString("88A7A905-CB27-431C-BFED-1D16BEA9B91C");
        Staff staff = null;
        try { staff = administratorService.getStaffByID(DEFAULT_STAFF_ID); } catch (Exception ignored) {}

        Customer customer = order.getCustomer();
        if (customer == null && user != null) customer = customerService.findCustomerByUsername(user.getUsername());

        if ("REJECT".equalsIgnoreCase(decision)) {

            if (customer != null && order.getTotalPrice() != null) {
                if (customer.getBalance() == null) customer.setBalance(BigDecimal.ZERO);
                customer.setBalance(customer.getBalance().add(order.getTotalPrice()));
                customerService.save(customer);

                Transaction tx = new Transaction();
                tx.setCustomer(customer);
                tx.setAmount(order.getTotalPrice());
                tx.setDescription("REJECT_ORDER_" + order.getId());
                tx.setDateCreated(LocalDateTime.now());
                transactionService.save(tx);
            }

            if (order.getVoucher() != null && customer != null) {
                try { voucherCustomerService.rollbackUsed(customer, order.getVoucher()); } catch (Exception ignored) {}
            }

            order.setStatus("REJECTED");
            if (staff != null) order.setStaff(staff);
            ordersService.save(order);

            if (customer != null && customer.getEmail() != null) {
                mailAsyncService.sendRejectEmail(customer.getEmail(), order.getId().toString(), order.getTotalPrice());
            }

            return "redirect:/staffHome/approveList";
        }

        List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);
        if (orderDetails == null || orderDetails.isEmpty()) return "redirect:/staffHome/approve/" + orderId;

        Map<UUID, UUID> selected = parseSelectedMap(params);

        if (selected.size() != orderDetails.size()) {
            Map<UUID, UUID> auto = autoPickAccounts(orderDetails);
            if (auto == null || auto.size() != orderDetails.size()) return "redirect:/staffHome/approve/" + orderId;
            selected = auto;
        } else {
            if (!validateSelectedMatchesOrderDetails(orderDetails, selected)) return "redirect:/staffHome/approve/" + orderId;
        }

        String accountHtml = handleAcceptAndPersist(order, orderDetails, selected);

        order.setStatus("COMPLETED");
        if (staff != null) order.setStaff(staff);
        ordersService.save(order);

//        if (customer != null && order.getTotalPrice() != null) {
//            Transaction tx = new Transaction();
//            tx.setCustomer(customer);
//            tx.setAmount(order.getTotalPrice().negate());
//            tx.setDescription("PAYMENT_COMPLETED_ORDER_" + order.getId());
//            tx.setDateCreated(LocalDateTime.now());
//            transactionService.save(tx);
//        }

        if (customer != null && customer.getEmail() != null) {
            mailAsyncService.sendAcceptEmail(customer.getEmail(), accountHtml);
        }

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
            } catch (Exception ignored) {}
        }

        return out;
    }

    private boolean validateSelectedMatchesOrderDetails(List<OrderDetail> orderDetails, Map<UUID, UUID> selected) {
        Set<UUID> used = new HashSet<>();
        for (OrderDetail od : orderDetails) {
            UUID gaId = selected.get(od.getId());
            if (gaId == null) return false;
            if (!used.add(gaId)) return false;

            GameAccount ga = gameAccountService.getGameById(gaId);
            if (ga == null) return false;

            if (ga.getStatus() == null || !ga.getStatus().equalsIgnoreCase("ACTIVE")) return false;
            if (!isMatch(od, ga)) return false;
        }
        return true;
    }

    private Map<UUID, UUID> autoPickAccounts(List<OrderDetail> orderDetails) {
        Map<UUID, UUID> selected = new LinkedHashMap<>();
        Set<UUID> used = new HashSet<>();

        for (OrderDetail od : orderDetails) {
            List<GameAccount> candidates = findCandidatesForOrderDetail(od);
            GameAccount pick = null;

            for (GameAccount ga : candidates) {
                if (ga != null && ga.getGameAccountId() != null && used.add(ga.getGameAccountId())) {
                    pick = ga;
                    break;
                }
            }

            if (pick == null) return null;
            selected.put(od.getId(), pick.getGameAccountId());
        }

        return selected;
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

        StringBuilder html = new StringBuilder();
        Customer customer = order.getCustomer();

        LocalDateTime baseTime = (order != null && order.getCreatedDate() != null) ? order.getCreatedDate() : LocalDateTime.now();

        for (OrderDetail od : orderDetails) {
            UUID gaId = selected.get(od.getId());
            if (gaId == null) continue;

            GameAccount ga = gameAccountService.getGameById(gaId);
            if (ga == null) continue;

            od.setGameAccount(ga);
            orderDetailService.save(od);

            boolean odRent = od.getDuration() != null && od.getDuration() > 0;

            if (!odRent) {
                gameOwnedService.createOwnedIfNotExists(customer, ga);
                ga.setStatus("SOLD");
                gameAccountService.save(ga);
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
            }

            html.append("<b>TRÒ CHƠI:</b> ").append(ga.getGame().getGameName()).append("<br>")
                    .append("<b>TÀI KHOẢN:</b> ").append(ga.getGameAccount()).append("<br>")
                    .append("<b>MẬT KHẨU:</b> ").append(ga.getGamePassword()).append("<br>")
                    .append("<b>HÌNH THỨC:</b> ").append(odRent ? ("THUÊ " + od.getDuration() + " THÁNG") : "MUA VĨNH VIỄN")
                    .append("<br>")
                    .append("<b>GIÁ:</b> ").append(ga.getPrice().toPlainString()).append(" đ<br><br>");
        }

        return html.toString();
    }
}
