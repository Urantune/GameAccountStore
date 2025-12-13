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
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.OrderDetail;
import webBackEnd.entity.Orders;
import webBackEnd.repository.CustomerRepositories;
import webBackEnd.repository.GameAccountRepositories;
import webBackEnd.repository.OrderDetailRepositories;
import webBackEnd.repository.OrdersRepositories;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameAccountService;
import webBackEnd.service.OrdersService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private GameAccountRepositories gameAccountRepositories;

    @GetMapping("/payment/{id}")
    public String checkout(@PathVariable("id") UUID id, Model model) {
        GameAccount game = gameAccountService.findGameAccountById(id);
        model.addAttribute("games", game);
        return "customer/Payment";
    }

    @Transactional
    @PostMapping("/order/confirm/{gameId}")
    public String confirmOrder(
            @PathVariable UUID gameId,
            @RequestParam("packageValues") String packageValues,
            Model model
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // ===== CHƯA LOGIN =====
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {

            model.addAttribute("errorMessage", "Bạn chưa đăng nhập!");
            model.addAttribute("games", getGame(gameId));
            return "customer/Payment";
        }

        Customer customer = customerService.findByCustomerUsername(authentication.getName());

        if (customer == null) {
            model.addAttribute("errorMessage", "Không tìm thấy customer!");
            model.addAttribute("games", getGame(gameId));
            return "customer/Payment";
        }

        GameAccount game = getGame(gameId);

        // ===== TÍNH GIÁ =====
        BigDecimal basePrice = game.getPrice();
        BigDecimal totalPrice = basePrice;

        if (packageValues.contains("2 Tháng")) {
            totalPrice = basePrice.multiply(BigDecimal.valueOf(0.9));
        } else if (packageValues.contains("3 Tháng")) {
            totalPrice = basePrice.multiply(BigDecimal.valueOf(0.85));
        }

        BigDecimal balance = customer.getBalance();

        // KHÔNG ĐỦ TIỀN
        if (balance.compareTo(totalPrice) < 0) {
            model.addAttribute("errorMessage", "Số dư không đủ để thực hiện giao dịch");
            model.addAttribute("games", game);
            return "customer/Payment";
        }

        // ✅ TRỪ TIỀN
        customer.setBalance(balance.subtract(totalPrice));
        customerRepositories.save(customer);

        // ===== ORDER =====
        Orders order = new Orders();
        order.setCustomer(customer);
        order.setTotalPrice(totalPrice);
        order.setCreatedDate(LocalDateTime.now());
        order.setStatus("WAIT");
        Orders savedOrder = ordersRepositories.save(order);

        // ===== ORDER DETAIL =====
        OrderDetail detail = new OrderDetail();
        detail.setOrder(savedOrder);
        detail.setGameAccount(game);
        detail.setDuration(
                packageValues.contains("1 Tháng") ? 1 :
                        packageValues.contains("2 Tháng") ? 2 :
                                packageValues.contains("3 Tháng") ? 3 : 0
        );
        orderDetailRepositories.save(detail);

        // ✅ THÀNH CÔNG
        model.addAttribute("successMessage", "Thanh toán thành công!");
        model.addAttribute("games", game);

        return "customer/Payment";
    }

    private GameAccount getGame(UUID gameId) {
        return gameAccountRepositories.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
    }




}
