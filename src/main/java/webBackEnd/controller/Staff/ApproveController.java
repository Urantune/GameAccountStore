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

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private GetQuantity getQuantity;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private AdministratorService administratorService;

    @Autowired
    private RentAccountGameService rentAccountGameService;

    @Autowired
    private GameAccountService gameAccountService;

    @Autowired
    private SendMailTest sendMailTest;

    @Autowired
    private GameService gameService;

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

    private Map<String, List<GameAccount>> initBucketsStr() {
        Map<String, List<GameAccount>> m = new LinkedHashMap<>();
        m.put("50000", new ArrayList<>());
        m.put("100000", new ArrayList<>());
        m.put("150000", new ArrayList<>());
        m.put("200000", new ArrayList<>());
        return m;
    }

    private String bucketKey(BigDecimal price) {
        if (price == null) return null;
        int p = price.intValue();
        if (p == 50000) return "50000";
        if (p == 100000) return "100000";
        if (p == 150000) return "150000";
        if (p == 200000) return "200000";
        return null;
    }

    private boolean isRent(GameAccount ga) {
        String d = ga.getDuration();
        return d != null && d.trim().equalsIgnoreCase("RENT");
    }

    private boolean isNullDuration(GameAccount ga) {
        String d = ga.getDuration();
        return d == null || d.trim().isEmpty();
    }

    @GetMapping("/approve/{orderId}")
    public String viewOrderDetail(@PathVariable UUID orderId, Model model) {



        Orders order = ordersService.findById(orderId);
        List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);

        Map<String, List<GameAccount>> aovNull = initBucketsStr();
        Map<String, List<GameAccount>> aovRent = initBucketsStr();
        Map<String, List<GameAccount>> ffNull = initBucketsStr();
        Map<String, List<GameAccount>> ffRent = initBucketsStr();

        List<GameAccount> listAOV = gameAccountService.findGameAccountByGame(
                gameService.findGameByGameName("AOV")
        );
        List<GameAccount> listFF = gameAccountService.findGameAccountByGame(
                gameService.findGameByGameName("FREE FIRE")
        );

        System.out.println("AOV raw=" + listAOV.size());
        System.out.println("FF raw=" + listFF.size());
        System.out.println("aovNull 50k=" + aovNull.get("50000").size() + ", aovRent 50k=" + aovRent.get("50000").size());
        System.out.println("ffNull 50k=" + ffNull.get("50000").size() + ", ffRent 50k=" + ffRent.get("50000").size());


        for (GameAccount ga : listAOV) {
            if (ga == null) continue;
            if (ga.getStatus() != null && !ga.getStatus().equalsIgnoreCase("ACTIVE")) continue;

            String k = bucketKey(ga.getPrice());
            if (k == null) continue;

            if (isRent(ga)) aovRent.get(k).add(ga);
            else if (isNullDuration(ga)) aovNull.get(k).add(ga);
        }

        for (GameAccount ga : listFF) {
            if (ga == null) continue;
            if (ga.getStatus() != null && !ga.getStatus().equalsIgnoreCase("ACTIVE")) continue;

            String k = bucketKey(ga.getPrice());
            if (k == null) continue;

            if (isRent(ga)) ffRent.get(k).add(ga);
            else if (isNullDuration(ga)) ffNull.get(k).add(ga);
        }

        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);

        model.addAttribute("aovNull", aovNull);
        model.addAttribute("aovRent", aovRent);
        model.addAttribute("ffNull", ffNull);
        model.addAttribute("ffRent", ffRent);

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

        if ("REJECT".equalsIgnoreCase(decision)) {
            order.setStatus("REJECTED");
            order.setStaff(administratorService.getStaffByID(UUID.fromString("88A7A905-CB27-431C-BFED-1D16BEA9B91B")));
            ordersService.save(order);

            Customer customer = (user != null) ? customerService.findCustomerByUsername(user.getUsername()) : order.getCustomer();
            if (customer != null) {
                customer.setBalance(customer.getBalance().add(order.getTotalPrice()));
                customerService.save(customer);
            }
            return "redirect:/staffHome/approveList";
        }

        List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);
        Map<UUID, UUID> selected = new HashMap<>();

        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            if (k != null && k.startsWith("selected[") && k.endsWith("]")) {
                String odIdStr = k.substring("selected[".length(), k.length() - 1);
                String gaIdStr = e.getValue();
                if (gaIdStr == null || gaIdStr.trim().isEmpty()) continue;
                try {
                    selected.put(UUID.fromString(odIdStr), UUID.fromString(gaIdStr));
                } catch (Exception ignored) {
                }
            }
        }

        for (OrderDetail od : orderDetails) {
            if (od == null) continue;
            UUID pick = selected.get(od.getId());
            if (pick == null) return "redirect:/staffHome/approve/" + orderId;
        }

        StringBuilder accountHtml = new StringBuilder();

        for (OrderDetail od : orderDetails) {
            UUID pick = selected.get(od.getId());
            GameAccount ga = gameAccountService.getGameById(pick);
            if (ga == null) return "redirect:/staffHome/approve/" + orderId;

            od.setGameAccount(ga);
            orderDetailService.save(od);

            if (od.getDuration() != null && od.getDuration() != 0) {
                RentAccountGame rentAccountGame = new RentAccountGame();
                rentAccountGame.setCustomer(order.getCustomer());
                rentAccountGame.setGameAccount(ga);
                rentAccountGame.setDateStart(order.getCreatedDate());
                rentAccountGame.setDateEnd(order.getCreatedDate().plusMonths(od.getDuration()));
                rentAccountGame.setStatus("Still valid");
                rentAccountGameService.save(rentAccountGame);
            }

            ga.setStatus("IN USE");
            gameAccountService.save(ga);

            accountHtml.append("<b>OrderDetail:</b> ").append(od.getId()).append("<br>")
                    .append("<b>Mode:</b> ").append((od.getDuration() != null && od.getDuration() != 0) ? ("RENT " + od.getDuration() + " month(s)") : "BUY").append("<br>")
                    .append("<b>game:</b> ").append(ga.getGame() != null ? ga.getGame().getGameName() : "N/A").append("<br>")
                    .append("<b>username:</b> ").append(ga.getGameAccount()).append("<br>")
                    .append("<b>password:</b> ").append(ga.getGamePassword()).append("<br>")
                    .append("<b>price:</b> ").append(ga.getPrice() != null ? ga.getPrice().toPlainString() : "N/A").append(" đ<br>")
                    .append("<br>");
        }

        order.setStatus("COMPLETED");
        order.setStaff(administratorService.getStaffByID(UUID.fromString("88A7A905-CB27-431C-BFED-1D16BEA9B91B")));
        ordersService.save(order);

        String title = "Xác nhận tài khoản của bạn";
        String content =
                "Xin chào <b>" + (order.getCustomer() != null ? order.getCustomer().getUsername() : "") + "</b>,<br><br>"
                        + "Đơn hàng của bạn đã được xử lý thành công.<br><br>"
                        + "Thông tin tài khoản:<br><br>"
                        + "<div style='padding:12px;border:1px solid #ddd;border-radius:8px;'>"
                        + accountHtml
                        + "</div><br>"
                        + "<b>LƯU Ý:</b><br>"
                        + "- Không chia sẻ thông tin tài khoản cho người khác.<br>"
                        + "- Nếu có lỗi, liên hệ trong vòng 24h để được hỗ trợ.<br><br>"
                        + "Trân trọng,<br><b>ACCOUNT GAME STORE</b>";

        if (order.getCustomer() != null && order.getCustomer().getEmail() != null) {
            sendMailTest.testSend(order.getCustomer().getEmail(), title, content);
        }

        return "redirect:/staffHome/approveList";
    }
}
