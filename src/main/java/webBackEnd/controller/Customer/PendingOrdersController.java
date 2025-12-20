package webBackEnd.controller.Customer;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.Customer;
import webBackEnd.entity.OrderDetail;
import webBackEnd.entity.Orders;
import webBackEnd.entity.Transaction;
import webBackEnd.repository.OrderDetailRepositories;
import webBackEnd.repository.OrdersRepositories;
import webBackEnd.repository.TransactionRepositories;
import webBackEnd.service.CustomerService;
import webBackEnd.service.TransactionService;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/home")
public class PendingOrdersController {

    @Autowired private CustomerService customerService;
    @Autowired private OrdersRepositories ordersRepositories;
    @Autowired private OrderDetailRepositories orderDetailRepositories;
    @Autowired private TransactionService transactionService;

    @GetMapping("/pendingOrders")
    public String pendingOrders(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null ? auth.getName() : null);

        if (username == null || "anonymousUser".equals(username)) {
            model.addAttribute("pendingOrders", new LinkedHashMap<Orders, List<OrderDetail>>());
            model.addAttribute("errorMessage", "Vui lòng đăng nhập.");
            return "customer/PendingOrders";
        }

        Customer customer = null;
        try { customer = customerService.findCustomerByUsername(username); } catch (Exception ignored) {}

        if (customer == null) {
            model.addAttribute("pendingOrders", new LinkedHashMap<Orders, List<OrderDetail>>());
            model.addAttribute("errorMessage", "Không tìm thấy thông tin khách hàng.");
            return "customer/PendingOrders";
        }

        List<Orders> all = ordersRepositories.findAll();

        List<Orders> pending = new ArrayList<>();
        for (Orders o : all) {
            if (o == null || o.getCustomer() == null || o.getCustomer().getCustomerId() == null) continue;
            if (!o.getCustomer().getCustomerId().equals(customer.getCustomerId())) continue;

            String st = o.getStatus();
            if (st == null) continue;

            if (st.equals("WAIT") || st.equals("WAITRENT")) pending.add(o);
        }

        pending.sort(Comparator.comparing(Orders::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        LinkedHashMap<Orders, List<OrderDetail>> map = new LinkedHashMap<>();
        for (Orders o : pending) {
            List<OrderDetail> details = orderDetailRepositories.findByOrder(o);
            if (details == null) details = List.of();
            map.put(o, details);
        }

        model.addAttribute("currentUser", customer);
        model.addAttribute("pendingOrders", map);
        return "customer/PendingOrders";
    }

    @PostMapping("/pendingOrders/cancel/{orderId}")
    @Transactional
    public String cancelPendingOrder(@PathVariable("orderId") UUID orderId, RedirectAttributes ra) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null ? auth.getName() : null);

        if (username == null || "anonymousUser".equals(username)) {
            ra.addFlashAttribute("errorMessage", "Bạn chưa đăng nhập.");
            return "redirect:/home/pendingOrders";
        }

        Customer customer = null;
        try { customer = customerService.findCustomerByUsername(username); } catch (Exception ignored) {}

        if (customer == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy thông tin khách hàng.");
            return "redirect:/home/pendingOrders";
        }

        Orders order = ordersRepositories.findById(orderId).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
            return "redirect:/home/pendingOrders";
        }

        if (order.getCustomer() == null || order.getCustomer().getCustomerId() == null
                || !order.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            ra.addFlashAttribute("errorMessage", "Bạn không có quyền hủy đơn này.");
            return "redirect:/home/pendingOrders";
        }

        String st = order.getStatus();
        if (st == null || !(st.equals("WAIT") || st.equals("WAITRENT"))) {
            ra.addFlashAttribute("errorMessage", "Đơn này không còn ở trạng thái chờ.");
            return "redirect:/home/pendingOrders";
        }

        order.setStatus("CANCEL");

          Transaction transaction = new Transaction();
          transaction.setCustomer(customer);
          transaction.setAmount(order.getTotalPrice());
          transaction.setDescription("CANCLE_COMLETED_ORDER" + order.getId());
          transaction.setDateCreated(LocalDateTime.now());
          transactionService.save(transaction);

        ordersRepositories.save(order);

        ra.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công.");
        return "redirect:/home/pendingOrders";
    }
}
