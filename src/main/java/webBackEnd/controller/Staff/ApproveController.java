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

    @Autowired
    private TransactionService transactionService;

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

        List<String> priceBuckets = Arrays.asList("50000", "100000", "150000", "200000");

        Map<String, List<GameAccount>> aovByPrice = new LinkedHashMap<>();
        Map<String, List<GameAccount>> ffByPrice = new LinkedHashMap<>();
        for (String p : priceBuckets) {
            aovByPrice.put(p, new ArrayList<>());
            ffByPrice.put(p, new ArrayList<>());
        }

        Game aov = gameService.findGameByGameName("AOV");
        Game ff = gameService.findGameByGameName("FREE FIRE");

        List<GameAccount> listAOV = gameAccountService.findGameAccountByGame(aov);
        List<GameAccount> listFF = gameAccountService.findGameAccountByGame(ff);

        for (GameAccount ga : listAOV) {
            if (ga.getPrice() == null) continue;

            String key = ga.getPrice().setScale(0).toPlainString();
            List<GameAccount> bucket = aovByPrice.get(key);
            if (bucket != null) bucket.add(ga);
        }

        for (GameAccount ga : listFF) {
            if (ga.getPrice() == null) continue;

            String key = ga.getPrice().setScale(0).toPlainString();
            List<GameAccount> bucket = ffByPrice.get(key);
            if (bucket != null) bucket.add(ga);
        }


        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("priceBuckets", priceBuckets);
        model.addAttribute("aovByPrice", aovByPrice);
        model.addAttribute("ffByPrice", ffByPrice);
        System.out.println("AOV bucket sizes: " + aovByPrice.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue().size()).toList());

        System.out.println("FF bucket sizes: " + ffByPrice.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue().size()).toList());



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
        try {
            staff = administratorService.getStaffByID(DEFAULT_STAFF_ID);
        } catch (Exception ignored) {}

        if ("REJECT".equalsIgnoreCase(decision)) {
            order.setStatus("REJECTED");
            if (staff != null) order.setStaff(staff);
            ordersService.save(order);

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
                tx.setDescription("Refund orderId=" + order.getId());
                tx.setDateCreated(LocalDateTime.now());
                transactionService.save(tx);
            }

            return "redirect:/staffHome/approveList";
        }

        List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);
        if (orderDetails == null || orderDetails.isEmpty()) return "redirect:/staffHome/approve/" + orderId;

        Map<UUID, UUID> selected = new HashMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            if (k != null && k.startsWith("selected[") && k.endsWith("]")) {
                String odIdStr = k.substring("selected[".length(), k.length() - 1);
                String gaIdStr = e.getValue();
                if (gaIdStr == null || gaIdStr.trim().isEmpty()) continue;
                try {
                    selected.put(UUID.fromString(odIdStr), UUID.fromString(gaIdStr));
                } catch (Exception ignored) {}
            }
        }

        Set<UUID> usedGa = new HashSet<>();
        for (OrderDetail od : orderDetails) {
            UUID pick = selected.get(od.getId());
            if (pick == null || !usedGa.add(pick)) return "redirect:/staffHome/approve/" + orderId;
        }

        java.util.function.Function<OrderDetail, String> odMode =
                od -> (od.getDuration() == null || od.getDuration() == 0) ? "BUY" : "RENT";
        java.util.function.Function<GameAccount, String> gaMode =
                ga -> (ga.getDuration() == null || ga.getDuration().trim().isEmpty() || "0".equals(ga.getDuration().trim()))
                        ? "BUY" : "RENT";

        StringBuilder accountHtml = new StringBuilder();

        for (OrderDetail od : orderDetails) {
            GameAccount ga = gameAccountService.getGameById(selected.get(od.getId()));
            if (ga == null) return "redirect:/staffHome/approve/" + orderId;

            od.setGameAccount(ga);
            orderDetailService.save(od);

            if (od.getDuration() != null && od.getDuration() != 0) {
                RentAccountGame rent = new RentAccountGame();
                rent.setCustomer(order.getCustomer());
                rent.setGameAccount(ga);
                rent.setDateStart(order.getCreatedDate());
                rent.setDateEnd(order.getCreatedDate().plusMonths(od.getDuration()));
                rent.setStatus("Still valid");
                rentAccountGameService.save(rent);
            }

            ga.setStatus("IN USE");
            gameAccountService.save(ga);

            accountHtml.append("<b>OrderDetail:</b> ").append(od.getId()).append("<br>")
                    .append("<b>Mode:</b> ").append(odMode.apply(od).equals("RENT") ? "RENT " + od.getDuration() + " month(s)" : "BUY").append("<br>")
                    .append("<b>Game:</b> ").append(ga.getGame() != null ? ga.getGame().getGameName() : "").append("<br>")
                    .append("<b>Username:</b> ").append(ga.getGameAccount()).append("<br>")
                    .append("<b>Password:</b> ").append(ga.getGamePassword()).append("<br>")
                    .append("<b>Price:</b> ").append(ga.getPrice() != null ? ga.getPrice().toPlainString() : "").append(" đ<br><br>");
        }

        order.setStatus("COMPLETED");
        if (staff != null) order.setStaff(staff);
        ordersService.save(order);

        Customer customer = order.getCustomer();
        if (customer != null && order.getTotalPrice() != null) {
            Transaction tx = new Transaction();
            tx.setCustomer(customer);
            tx.setAmount(order.getTotalPrice().negate());
            tx.setDescription("Payment completed orderId=" + order.getId());
            tx.setDateCreated(LocalDateTime.now());
            transactionService.save(tx);
        }

        if (order.getCustomer() != null && order.getCustomer().getEmail() != null) {
            sendMailTest.testSend(
                    order.getCustomer().getEmail(),
                    "Account confirmation",
                    accountHtml.toString()
            );
        }

        return "redirect:/staffHome/approveList";
    }



}
