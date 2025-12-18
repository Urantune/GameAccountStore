package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.Customer;
import webBackEnd.entity.OrderDetail;
import webBackEnd.entity.Orders;
import webBackEnd.service.CustomerService;
import webBackEnd.service.OrderDetailService;
import webBackEnd.service.OrdersService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/home")
public class ListAccController {

    @Autowired private CustomerService customerService;
    @Autowired private OrdersService ordersService;
    @Autowired private OrderDetailService orderDetailService;

    @GetMapping("/listAcc")
    public String listAcc(@RequestParam(value = "game", required = false) String game, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null ? auth.getName() : null);

        if (username == null || "anonymousUser".equals(username)) {
            model.addAttribute("listAcc", List.of());
            model.addAttribute("errorMessage", "Vui lòng đăng nhập.");
            model.addAttribute("gameName", game);
            return "customer/ListAcc";
        }

        Customer customer = null;
        try { customer = customerService.findCustomerByUsername(username); } catch (Exception ignored) {}

        if (customer == null) {
            model.addAttribute("listAcc", List.of());
            model.addAttribute("errorMessage", "Không tìm thấy thông tin khách hàng.");
            model.addAttribute("gameName", game);
            return "customer/ListAcc";
        }

        List<Orders> orders = ordersService.findAll();
        List<OrderDetail> result = new ArrayList<>();

        for (Orders o : orders) {
            if (o == null || o.getCustomer() == null || o.getCustomer().getCustomerId() == null) continue;
            if (!o.getCustomer().getCustomerId().equals(customer.getCustomerId())) continue;

            String st = o.getStatus();
            if (st == null) continue;
            if (!(st.equals("COMPLETED") || st.equals("COMPLETED"))) continue;

            List<OrderDetail> details = orderDetailService.getOrderDetailByOrder(o);
            if (details == null || details.isEmpty()) continue;

            for (OrderDetail od : details) {
                if (od == null) continue;
                if (od.getGameAccount() == null) continue;

                if (game != null && !game.isBlank()) {
                    if (od.getGame() == null || od.getGame().getGameName() == null) continue;
                    if (!od.getGame().getGameName().equalsIgnoreCase(game.trim())) continue;
                }

                result.add(od);
            }
        }

        result.sort(Comparator.comparing((OrderDetail od) ->
                        od.getOrder() != null ? od.getOrder().getCreatedDate() : null,
                Comparator.nullsLast(Comparator.naturalOrder())
        ).reversed());

        model.addAttribute("currentUser", customer);
        model.addAttribute("gameName", game);
        model.addAttribute("listAcc", result);
        return "customer/ListAcc";
    }
}
