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
import webBackEnd.service.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping(value = "/home")
public class BuyController {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomerRepositories customerRepositories;

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
    @Autowired
    private VoucherCustomerRepository voucherCustomerRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CartService cartService;


    @GetMapping("/payment/cart")
    public String paymentCart(@RequestParam("ids") String ids, Model model) {
        var list = Arrays.stream(ids.split(","))
                .filter(s -> !s.isBlank())
                .map(UUID::fromString)
                .map(gameAccountService::findGameAccountById)
                .filter(Objects::nonNull)
                .toList();

        model.addAttribute("gamesList", list);
        model.addAttribute("idsStr", ids);
        return "customer/PaymentCart";
    }

    @PostMapping("/payment")
    @Transactional
    public String buyRandomAccount(
            @RequestParam String accountType,               // permanent | rent
            @RequestParam(defaultValue = "0") int rentMonth, // 0 | 1 | 2 | 3
            @RequestParam String skinRange,                  // vd: 20 - 30 skin
            @RequestParam BigDecimal finalPrice,             // giá cuối cùng
            @RequestParam(required = false) String voucherCode,
            RedirectAttributes ra
    ) {
        System.out.println("DEBUG: voucherCode raw = '" + voucherCode + "'");

        // ===================== 1. CHECK LOGIN =====================
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            ra.addFlashAttribute("error", "Bạn chưa đăng nhập");
            return "redirect:/login";
        }

        Customer customer = customerService
                .findCustomerByUsername(auth.getName());

        // ===================== 2. VALIDATE INPUT =====================
        if (finalPrice == null || finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("error", "Giá không hợp lệ");
            return "redirect:/home";
        }

        if ("rent".equals(accountType) && rentMonth <= 0) {
            ra.addFlashAttribute("error", "Vui lòng chọn gói thuê");
            return "redirect:/home";
        }

        // ===================== 3. XỬ LÝ VOUCHER =====================
        Voucher usedVoucher = null;

        if (voucherCode != null && !voucherCode.isBlank()) {
            String code = voucherCode.trim();
            usedVoucher = voucherService.getValidVoucher(voucherCode);

            if (usedVoucher == null) {
                ra.addFlashAttribute("error", "Voucher không hợp lệ hoặc đã hết hạn");
                return "redirect:/home";
            }

            boolean used = voucherCustomerRepository
                    .existsByCustomerAndVoucher(customer, usedVoucher);

            if (used) {
                ra.addFlashAttribute("error", "Voucher đã được sử dụng");
                return "redirect:/home";
            }
        }

        // ===================== 4. CHECK SỐ DƯ =====================
        if (customer.getBalance().compareTo(finalPrice) < 0) {
            ra.addFlashAttribute("error", "Số dư không đủ");
            return "redirect:/home";
        }

        // ===================== 5. TRỪ TIỀN =====================
        customer.setBalance(customer.getBalance().subtract(finalPrice));
        customerRepositories.save(customer);

        // ===================== 6. LƯU ORDERS =====================
        Orders order = new Orders();
        order.setCustomer(customer);
        order.setTotalPrice(finalPrice);
        order.setStatus("WAIT");

        if (usedVoucher != null) {
            order.setVoucher(usedVoucher);
        }

        Orders savedOrder = ordersRepositories.save(order);

        // ===================== 7. LƯU ORDER DETAIL =====================
        OrderDetail detail = new OrderDetail();
        detail.setOrder(savedOrder);

        // ❗ RANDOM ACCOUNT → CHƯA GÁN ACC
        detail.setGameAccount(null);

        detail.setDuration(
                "rent".equals(accountType) ? rentMonth : 0
        );

        detail.setPrice(finalPrice.intValue());

        orderDetailRepositories.save(detail);

        // ===================== 8. LƯU VOUCHER CUSTOMER =====================
        if (usedVoucher != null) {
            VoucherCustomer vc = new VoucherCustomer();
            vc.setCustomer(customer);
            vc.setVoucher(usedVoucher);
            vc.setDateUsed(LocalDateTime.now());

            voucherCustomerRepository.save(vc);
        }

        // ===================== 9. SUCCESS =====================
        ra.addFlashAttribute(
                "success",
                "Mua acc random thành công! Vui lòng chờ admin xử lý."
        );

        return "redirect:/home";
    }


    private void deleteCartItemsOfCustomer(Customer customer, List<UUID> gameIds) {
        var carts = cartService.getCartsByCustomer(customer);
        if (carts == null || carts.isEmpty()) return;

        Set<UUID> set = new HashSet<>(gameIds);

        for (Cart c : carts) {
            if (c.getGameAccount() != null && c.getGameAccount().getId() != null) {
                if (set.contains(c.getGameAccount().getId())) {
                    cartService.delete(c);
                }
            }
        }
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
