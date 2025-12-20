package webBackEnd.controller.Customer;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.*;
import webBackEnd.repository.*;
import webBackEnd.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/home")
public class RentController {

    @Autowired private CustomerRepositories customerRepositories;
    @Autowired private RentAccountGameRepositories rentAccountGameRepositories;
    @Autowired private OrdersRepositories ordersRepositories;
    @Autowired private OrderDetailRepositories orderDetailRepositories;
    @Autowired private TransactionService transactionService;

    @GetMapping("/renting")
    public String rentingAccounts(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null ? auth.getName() : null);

        if (username == null || "anonymousUser".equals(username)) {
            model.addAttribute("rentList", List.of());
            model.addAttribute("errorMessage", "Vui lòng đăng nhập.");
            return "customer/RentingAccounts";
        }

        Customer customer = null;
        try { customer = customerRepositories.findByUsername(username); } catch (Exception ignored) {}

        if (customer == null) {
            model.addAttribute("rentList", List.of());
            model.addAttribute("errorMessage", "Không tìm thấy thông tin khách hàng.");
            return "customer/RentingAccounts";
        }

        List<RentAccountGame> rentList = rentAccountGameRepositories.findAllByCustomer(customer);
        model.addAttribute("rentList", rentList);
        return "customer/RentingAccounts";
    }

    @PostMapping("/renting/renew")
    public String renewRent(@RequestParam("rentGameId") UUID rentGameId,
                            @RequestParam("months") Integer months,
                            RedirectAttributes ra) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null ? auth.getName() : null);

        if (username == null || "anonymousUser".equals(username)) {
            ra.addFlashAttribute("errorMessage", "Vui lòng đăng nhập.");
            return "redirect:/home/renting";
        }

        Customer customer = null;
        try { customer = customerRepositories.findByUsername(username); } catch (Exception ignored) {}

        if (customer == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy thông tin khách hàng.");
            return "redirect:/home/renting";
        }

        if (months == null || months < 1 || months > 3) {
            ra.addFlashAttribute("errorMessage", "Chỉ được chọn gia hạn 1 - 3 tháng.");
            return "redirect:/home/renting";
        }

        Optional<RentAccountGame> optRent = rentAccountGameRepositories.findById(rentGameId);
        if (optRent.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy gói thuê.");
            return "redirect:/home/renting";
        }

        RentAccountGame rent = optRent.get();

        if (rent.getCustomer() == null || rent.getCustomer().getCustomerId() == null ||
                !rent.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            ra.addFlashAttribute("errorMessage", "Không có quyền gia hạn gói thuê này.");
            return "redirect:/home/renting";
        }

        GameAccount ga = rent.getGameAccount();
        if (ga == null) {
            ra.addFlashAttribute("errorMessage", "Gói thuê không có game account.");
            return "redirect:/home/renting";
        }

        BigDecimal base = (ga.getPrice() != null) ? ga.getPrice() : BigDecimal.ZERO;
        BigDecimal subtotal = base.multiply(BigDecimal.valueOf(months));

        BigDecimal discountRate = BigDecimal.ONE;
        if (months == 2) discountRate = new BigDecimal("0.90");
        if (months == 3) discountRate = new BigDecimal("0.85");

        BigDecimal total = subtotal.multiply(discountRate).setScale(0, java.math.RoundingMode.HALF_UP);

        Orders order = new Orders();
        order.setCustomer(customer);
        order.setTotalPrice(total);
        order.setStatus("WAITRENT");
        Orders savedOrder = ordersRepositories.save(order);

        OrderDetail od = new OrderDetail();
        od.setOrder(savedOrder);
        od.setGameAccount(ga);
        od.setGame(null);
        od.setDuration(months);
        od.setPrice(total.intValue());
        orderDetailRepositories.save(od);

        customer.setBalance(customer.getBalance().subtract(order.getTotalPrice()));
        customerRepositories.save(customer);

        Transaction tx = new Transaction();
        tx.setCustomer(customer);
        tx.setAmount(order.getTotalPrice().negate());
        tx.setDescription("PAYMENT_COMPLETED_RENT_ORDER_" + order.getId());
        tx.setDateCreated(LocalDateTime.now());
        transactionService.save(tx);

        ra.addFlashAttribute("successMessage", "Đã tạo đơn gia hạn. Vui lòng thanh toán để staff duyệt.");
        return "redirect:/home/renting";
    }

    @PostMapping("/renting/pay")
    @Transactional
    public String payRent(@RequestParam("orderId") UUID orderId,
                          RedirectAttributes ra) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null ? auth.getName() : null);

        if (username == null || "anonymousUser".equals(username)) {
            ra.addFlashAttribute("errorMessage", "Vui lòng đăng nhập.");
            return "redirect:/home/renting";
        }

        Customer customer = null;
        try { customer = customerRepositories.findByUsername(username); } catch (Exception ignored) {}

        if (customer == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy thông tin khách hàng.");
            return "redirect:/home/renting";
        }

        Orders order = ordersRepositories.findById(orderId).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy đơn.");
            return "redirect:/home/renting";
        }

        if (order.getCustomer() == null || order.getCustomer().getCustomerId() == null ||
                !order.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            ra.addFlashAttribute("errorMessage", "Không có quyền thanh toán đơn này.");
            return "redirect:/home/renting";
        }

        if (!"PENDINGPAY_RENT".equalsIgnoreCase(order.getStatus())) {
            ra.addFlashAttribute("errorMessage", "Đơn không ở trạng thái chờ thanh toán.");
            return "redirect:/home/renting";
        }

        if (order.getTotalPrice() == null) {
            ra.addFlashAttribute("errorMessage", "Đơn thiếu tổng tiền.");
            return "redirect:/home/renting";
        }

        if (customer.getBalance() == null) customer.setBalance(BigDecimal.ZERO);

        if (customer.getBalance().compareTo(order.getTotalPrice()) < 0) {
            ra.addFlashAttribute("errorMessage", "Số dư không đủ.");
            return "redirect:/home/renting";
        }

        customer.setBalance(customer.getBalance().subtract(order.getTotalPrice()));
        customerRepositories.save(customer);

        Transaction tx = new Transaction();
        tx.setCustomer(customer);
        tx.setAmount(order.getTotalPrice().negate());
        tx.setDescription("PAYMENT_COMPLETED_RENT_ORDER_" + order.getId());
        tx.setDateCreated(LocalDateTime.now());
        transactionService.save(tx);

        order.setStatus("WAITRENT");
        ordersRepositories.save(order);

        ra.addFlashAttribute("successMessage", "Thanh toán thành công. Chờ staff duyệt.");
        return "redirect:/home/renting";
    }
}
