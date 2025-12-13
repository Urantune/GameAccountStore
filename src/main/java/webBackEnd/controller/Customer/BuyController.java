package webBackEnd.controller.Customer;

import jakarta.servlet.http.HttpSession;
import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.OrderDetail;
import webBackEnd.entity.Orders;
import webBackEnd.repository.CustomerRepositories;
import webBackEnd.repository.GameAccountRepositories;
import webBackEnd.repository.OrderDetailRepositories;
import webBackEnd.repository.OrdersRepositories;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameAccountService;
import webBackEnd.service.OrderDetailService;
import webBackEnd.service.OrdersService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(value = "/home")
public class BuyController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomerRepositories customerRepositories;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrdersRepositories ordersRepositories;
    @Autowired
    private OrderDetailRepositories orderDetailRepositories;
    @Autowired
    private GameAccountService gameAccountService;
    @Autowired
    private GameAccountRepositories  gameAccountRepositories;

    @GetMapping("/payment/{id}")
    public String checkout(@PathVariable("id") UUID id, Model model){
        GameAccount game = gameAccountService.findGameAccountById(id);
        model.addAttribute("games", game);
        return "customer/Payment";
    }

    @PostMapping("/order/confirm/{gameId}")
    public String confirmOrder(@PathVariable UUID gameId, @RequestParam("packageValues") String packageValues, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }

        String username = authentication.getName();
        Customer customer = customerService.findByCustomerUsername(username);

        if (customer == null) {
            throw new RuntimeException("Không tìm thấy customer!");
        }

        GameAccount game = gameAccountRepositories.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        // ===== TÍNH GIÁ Ở BACKEND (NGUỒN SỰ THẬT) =====
        BigDecimal basePrice = game.getPrice();
        BigDecimal totalPrice = basePrice;

        if (packageValues.contains("2 Tháng")) {
            totalPrice = basePrice.multiply(new BigDecimal("0.9"));
        } else if (packageValues.contains("3 Tháng")) {
            totalPrice = basePrice.multiply(new BigDecimal("0.85"));
        }


        // ===== TẠO ORDER =====
        Orders order = new Orders();
        order.setCustomer(customer);
        order.setVoucher(null);
        order.setStaff(null);
        order.setTotalPrice(totalPrice);
        order.setType(true);
        order.setCreatedDate(LocalDateTime.now());
        order.setStatus("WAIT");

        Orders savedOrder = ordersRepositories.save(order);
        // ===== ORDER DETAIL =====
        OrderDetail detail = new OrderDetail();
        detail.setOrder(savedOrder);
        detail.setGameAccount(game);
        orderDetailRepositories.save(detail);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Thanh toán thành công! Vui lòng kiểm tra lịch sử giao dịch."
        );
        return "redirect:/home";
    }


}
