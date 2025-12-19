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
import webBackEnd.successfullyDat.SendMailTest;

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
    @Autowired private SendMailTest sendMailTest;
    @Autowired private GameService gameService;
    @Autowired private TransactionService transactionService;

    @GetMapping("/approveList")
    public String approveList(Model model) {
        List<Orders> list = ordersService.findAllByStatus("WAIT");
        list.sort(Comparator.comparing(Orders::getCreatedDate));

        Map<UUID, List<OrderDetail>> orderDetailsMap = new HashMap<>();
        for (Orders o : list) {
            orderDetailsMap.put(o.getId(), orderDetailService.findAllByOrderId(o.getId()));
        }

        model.addAttribute("orderList", list);
        model.addAttribute("orderDetailsMap", orderDetailsMap);
        model.addAttribute("getQuantity", getQuantity);

        return "staff/ApproveList";
    }

    @GetMapping("/approve/{orderId}")
    public String viewOrderDetail(@PathVariable UUID orderId, Model model) {
        Orders order = ordersService.findById(orderId);
        List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);

        Map<UUID, List<GameAccount>> candidatesMap = new LinkedHashMap<>();
        if (orderDetails != null) {
            for (OrderDetail od : orderDetails) {
                candidatesMap.put(od.getId(), findCandidatesForOrderDetail(od));
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
                           @AuthenticationPrincipal CustomUserDetails user) {

        Orders order = ordersService.findById(orderId);
        if (order == null) return "redirect:/staffHome/approveList";

        final UUID DEFAULT_STAFF_ID = UUID.fromString("88A7A905-CB27-431C-BFED-1D16BEA9B91C");
        Staff staff = null;
        try {
            staff = administratorService.getStaffByID(DEFAULT_STAFF_ID);
        } catch (Exception ignored) {}

        if ("REJECT".equalsIgnoreCase(decision)) {

            Customer customer = (user != null)
                    ? customerService.findCustomerByUsername(user.getUsername())
                    : order.getCustomer();

            if (customer != null && order.getTotalPrice() != null) {
                if (customer.getBalance() == null) customer.setBalance(BigDecimal.ZERO);
                customer.setBalance(customer.getBalance().add(order.getTotalPrice()));
                customerService.save(customer);

                Transaction tx = new Transaction();
                tx.setCustomer(customer);
                tx.setAmount(order.getTotalPrice());
                tx.setDescription("REJECT");
                tx.setDateCreated(LocalDateTime.now());
                transactionService.save(tx);
            }

            List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);
            for (OrderDetail od : orderDetails) {
                orderDetailService.delete(od);
            }

            ordersService.delete(order);
            sendRejectEmail(order.getCustomer(), order);

            return "redirect:/staffHome/approveList";
        }

        List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);
        if (orderDetails == null || orderDetails.isEmpty()) return "redirect:/staffHome/approve/" + orderId;

        Map<UUID, UUID> selected = autoPickAccounts(orderDetails);
        if (selected == null || selected.size() != orderDetails.size()) return "redirect:/staffHome/approve/" + orderId;

        String accountHtml = handleAcceptAndBuildMail(order, orderDetails, selected);

        order.setStatus("COMPLETED");
        if (staff != null) order.setStaff(staff);
        ordersService.save(order);

        Customer customer = order.getCustomer();
        if (customer != null && order.getTotalPrice() != null) {
            Transaction tx = new Transaction();
            tx.setCustomer(customer);
            tx.setAmount(order.getTotalPrice().negate());
            tx.setDescription("PAYMENT_COMPLETED_ORDER_" + order.getId());
            tx.setDateCreated(LocalDateTime.now());
            transactionService.save(tx);
        }

        sendAcceptEmail(order.getCustomer(), accountHtml);

        return "redirect:/staffHome/approveList";
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

    private List<GameAccount> findCandidatesForOrderDetail(OrderDetail od) {
        if (od == null || od.getGame() == null || od.getPrice() == null) return List.of();

        Game game = od.getGame();
        List<GameAccount> all = gameAccountService.findGameAccountByGame(game);
        if (all == null || all.isEmpty()) return List.of();

        String odRank = od.getRank();
        Integer odPriceInt = od.getPrice();

        BigDecimal odPrice = BigDecimal.valueOf(odPriceInt.longValue());

        List<GameAccount> out = new ArrayList<>();
        for (GameAccount ga : all) {
            if (ga == null) continue;

            if (ga.getStatus() == null || !ga.getStatus().equalsIgnoreCase("ACTIVE")) continue;

            if (ga.getPrice() == null || ga.getPrice().compareTo(odPrice) != 0) continue;
            if (ga.getVip() != od.getVip()) continue;
            if (ga.getLovel() != od.getLovel()) continue;
            if (ga.getSkin() != od.getSkin()) continue;

            if (odRank != null && !odRank.isBlank()) {
                if (ga.getRank() == null) continue;
                if (!ga.getRank().equalsIgnoreCase(odRank)) continue;
            }

            out.add(ga);
        }

        out.sort(Comparator.comparing(GameAccount::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return out;
    }

    private String handleAcceptAndBuildMail(Orders order,
                                            List<OrderDetail> orderDetails,
                                            Map<UUID, UUID> selected) {

        StringBuilder html = new StringBuilder();

        for (OrderDetail od : orderDetails) {
            GameAccount ga = gameAccountService.getGameById(selected.get(od.getId()));
            if (ga == null) continue;

            od.setGameAccount(ga);
            orderDetailService.save(od);

            if (od.getDuration() != null && od.getDuration() > 0) {
                RentAccountGame rent = new RentAccountGame();
                rent.setCustomer(order.getCustomer());
                rent.setGameAccount(ga);
                rent.setDateStart(order.getCreatedDate());
                rent.setDateEnd(order.getCreatedDate().plusMonths(od.getDuration()));
                rent.setStatus("STILL VALID");
                rentAccountGameService.save(rent);
            }

            ga.setStatus("IN USE");
            gameAccountService.save(ga);

            html.append("<b>TRÒ CHƠI:</b> ").append(ga.getGame().getGameName()).append("<br>")
                    .append("<b>TÀI KHOẢN:</b> ").append(ga.getGameAccount()).append("<br>")
                    .append("<b>MẬT KHẨU:</b> ").append(ga.getGamePassword()).append("<br>")
                    .append("<b>HÌNH THỨC:</b> ")
                    .append(od.getDuration() != null && od.getDuration() > 0
                            ? "THUÊ " + od.getDuration() + " THÁNG"
                            : "MUA VĨNH VIỄN")
                    .append("<br>")
                    .append("<b>GIÁ:</b> ").append(ga.getPrice().toPlainString()).append(" đ<br><br>");
        }

        return html.toString();
    }

    private void sendRejectEmail(Customer customer, Orders order) {
        if (customer == null || customer.getEmail() == null) return;

        String body =
                "<b>Kính chào quý khách,</b><br><br>" +
                        "Đơn hàng của bạn đã bị <b>TỪ CHỐI</b> do hiện tại cửa hàng đã <b>HẾT TÀI KHOẢN PHÙ HỢP</b>.<br><br>" +
                        "<b>MÃ ĐƠN HÀNG:</b> " + order.getId() + "<br><br>" +
                        "Số tiền đã được hoàn lại vào ví của bạn.<br>" +
                        "Vui lòng quay lại sau hoặc liên hệ hỗ trợ để được tư vấn thêm.<br><br>" +
                        "Xin cảm ơn.";

        sendMailTest.testSend(customer.getEmail(), "ĐƠN HÀNG BỊ TỪ CHỐI", body);
    }

    private void sendAcceptEmail(Customer customer, String accountHtml) {
        if (customer == null || customer.getEmail() == null) return;

        String body =
                "<b>Kính chào quý khách,</b><br><br>" +
                        "Đơn hàng của bạn đã được <b>XÁC NHẬN THÀNH CÔNG</b>.<br>" +
                        "Dưới đây là thông tin tài khoản game bạn đã mua / thuê:<br><br>" +
                        accountHtml +
                        "<hr>" +
                        "<b>LƯU Ý QUAN TRỌNG:</b><br>" +
                        "- Vui lòng <b>ĐỔI MẬT KHẨU NGAY</b> sau khi đăng nhập.<br>" +
                        "- Không chia sẻ thông tin tài khoản cho người khác.<br>" +
                        "- Với tài khoản thuê, hệ thống sẽ <b>TỰ ĐỘNG THU HỒI</b> khi hết hạn.<br>" +
                        "- Mọi khiếu nại vui lòng liên hệ trong vòng <b>24 GIỜ</b> kể từ khi nhận tài khoản.<br><br>" +
                        "Xin cảm ơn và chúc bạn chơi game vui vẻ.";

        sendMailTest.testSend(customer.getEmail(), "THÔNG TIN TÀI KHOẢN GAME", body);
    }
}
