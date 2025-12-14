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
import webBackEnd.repository.CustomerRepositories;
import webBackEnd.repository.GameAccountRepositories;
import webBackEnd.repository.OrderDetailRepositories;
import webBackEnd.repository.OrdersRepositories;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameAccountService;
import webBackEnd.service.OrdersService;
import webBackEnd.service.VoucherService;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @Autowired
    private VoucherService voucherService;

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
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            Model model
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            model.addAttribute("errorMessage", "Bạn chưa đăng nhập!");
            model.addAttribute("games", getGame(gameId));
            return "customer/Payment";
        }

        Customer customer = customerService.findByCustomerUsername(authentication.getName());
        GameAccount game = getGame(gameId);

        if (packageValues == null || packageValues.isBlank()) {
            model.addAttribute("errorMessage", "Vui lòng chọn gói thuê");
            model.addAttribute("games", game);
            return "customer/Payment";
        }
        // Tính giá gốc
        BigDecimal totalPrice = game.getPrice();

        if (packageValues.contains("2 Tháng")) {
            totalPrice = totalPrice.multiply(BigDecimal.valueOf(0.9));
        } else if (packageValues.contains("3 Tháng")) {
            totalPrice = totalPrice.multiply(BigDecimal.valueOf(0.85));
        }

        //voucher
        Voucher voucher = null;
        if (voucherCode != null && !voucherCode.isBlank()) {
             voucher = voucherService.getValidVoucher(voucherCode);
            if (voucher == null) {
                model.addAttribute("errorMessage", "Voucher không hợp lệ hoặc đã hết hạn");
                model.addAttribute("games", game);
                return "customer/Payment";
            }

            BigDecimal discountPercent =
                    BigDecimal.valueOf(voucher.getValue()).divide(BigDecimal.valueOf(100));
            totalPrice = totalPrice.subtract(totalPrice.multiply(discountPercent));
        }

        totalPrice = totalPrice.setScale(0, RoundingMode.HALF_UP);

        //Check tiền
        if (customer.getBalance().compareTo(totalPrice) < 0) {
            model.addAttribute("errorMessage", "Số dư không đủ");
            model.addAttribute("games", game);
            return "customer/Payment";
        }

        //Trừ
        customer.setBalance(customer.getBalance().subtract(totalPrice));
        customerRepositories.save(customer);

        //Orders
        Orders order = new Orders();
        order.setCustomer(customer);
        order.setTotalPrice(totalPrice);
        order.setCreatedDate(LocalDateTime.now());
        order.setStatus("WAIT");
        if (voucher != null) {
            order.setVoucher(voucher);
        }
        Orders savedOrder = ordersRepositories.save(order);

        //OrdersDetail
        OrderDetail detail = new OrderDetail();
        detail.setOrder(savedOrder);
        detail.setGameAccount(game);
        detail.setDuration(
                packageValues.contains("1 Tháng") ? 1 :
                        packageValues.contains("2 Tháng") ? 2 :
                                packageValues.contains("3 Tháng") ? 3 : 0
        );
        orderDetailRepositories.save(detail);
        model.addAttribute("successMessage", "Thanh toán thành công!");
        model.addAttribute("games", game);

        return "customer/Payment";
    }

    private GameAccount getGame(UUID gameId) {
        return gameAccountRepositories.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
    }

    @ModelAttribute("currentUser")
    public Customer currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerService.findCustomerByUsername(username);
    }

}
