package webBackEnd.controller.Customer;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @Autowired
    private GameService gameService;


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



    @GetMapping("/api/voucher/check")
    @ResponseBody
    public Map<String, Object> checkVoucher(@RequestParam String code) {

        Map<String, Object> rs = new HashMap<>();
        String c = (code == null) ? "" : code.trim();

        Voucher v = voucherService.getValidVoucher(c);
        if (v == null) {
            rs.put("valid", false);
            rs.put("percent", 0);
            rs.put("used", false);
            return rs;
        }

        rs.put("valid", true);
        rs.put("percent", v.getValue()); // sửa đúng field % của Voucher bạn

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean logged = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());

        if (!logged) {
            rs.put("used", false);
            return rs;
        }

        Customer customer = customerService.findCustomerByUsername(auth.getName());
        boolean used = voucherCustomerRepository.existsByCustomerAndVoucher(customer, v);

        rs.put("used", used);
        return rs;
    }



    @PostMapping("/payment")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> buyRandomAccount(
            @RequestParam UUID gameId,
            @RequestParam String accountType,
            @RequestParam(defaultValue = "0") int rentMonth,
            @RequestParam BigDecimal basePrice,
            @RequestParam BigDecimal finalPrice,
            @RequestParam(required = false) String voucherCode
    ) {

        // ===================== 1. CHECK LOGIN =====================
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", "Bạn chưa đăng nhập"
                    ));
        }

        Customer customer = customerService.findCustomerByUsername(auth.getName());

        // ===================== 2. VALIDATE INPUT =====================
        if (finalPrice == null || finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Giá không hợp lệ"
                    ));
        }

        if ("rent".equals(accountType) && rentMonth <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Vui lòng chọn gói thuê"
                    ));
        }

        // ===================== 3. XỬ LÝ VOUCHER =====================
        Voucher usedVoucher = null;

        if (voucherCode != null && !voucherCode.isBlank()) {
            usedVoucher = voucherService.getValidVoucher(voucherCode.trim());

            if (usedVoucher == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Voucher không hợp lệ hoặc đã hết hạn"
                        ));
            }

            if (voucherCustomerRepository.existsByCustomerAndVoucher(customer, usedVoucher)) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Voucher đã được sử dụng"
                        ));
            }
        }

        // ===================== 4. CHECK SỐ DƯ =====================
        if (customer.getBalance().compareTo(finalPrice) < 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Số dư không đủ"
                    ));
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
        detail.setGame(gameService.findById(gameId));
        detail.setOrder(savedOrder);
        detail.setGameAccount(null); // RANDOM
        detail.setDuration("rent".equals(accountType) ? rentMonth : 0);
        detail.setPrice(basePrice.intValue());

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
        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Mua acc random thành công! Vui lòng chờ admin xử lý."
                )
        );
    }



    private void deleteCartItemsOfCustomer(Customer customer, List<UUID> gameIds) {
        var carts = cartService.getCartsByCustomer(customer);
        if (carts == null || carts.isEmpty()) return;

        Set<UUID> set = new HashSet<>(gameIds);

        for (Cart c : carts) {
            if (c.getGame() != null && c.getGame().getGameId() != null) {
                if (set.contains(c.getGame().getGameId())) {
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
