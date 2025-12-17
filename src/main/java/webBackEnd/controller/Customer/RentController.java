package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.*;
import webBackEnd.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping(value = "/home")
public class RentController {

    @Autowired private CustomerRepositories customerRepositories;
    @Autowired private RentAccountGameRepositories rentAccountGameRepositories;
    @Autowired private OrdersRepositories ordersRepositories;
    @Autowired private OrderDetailRepositories orderDetailRepositories;

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

        if (rent.getCustomer() == null || !rent.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            ra.addFlashAttribute("errorMessage", "Không có quyền gia hạn gói thuê này.");
            return "redirect:/home/renting";
        }

        GameAccount ga = rent.getGameAccount();
        if (ga == null) {
            ra.addFlashAttribute("errorMessage", "Gói thuê không có game account.");
            return "redirect:/home/renting";
        }

        BigDecimal base = BigDecimal.ZERO;
        if (ga.getPrice() != null) base = ga.getPrice();

        BigDecimal total = base.multiply(BigDecimal.valueOf(months));

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

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = rent.getDateEnd();
        if (end == null || end.isBefore(now)) end = now;

        rent.setDateEnd(end.plusMonths(months));
        rentAccountGameRepositories.save(rent);

        ra.addFlashAttribute("successMessage", "Gia hạn +" + months + " tháng. Đã tạo đơn WAITRENT.");
        return "redirect:/home/renting";
    }

}
