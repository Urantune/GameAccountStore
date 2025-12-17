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

    @GetMapping("/payment")
    public String confirmCart(
            @RequestParam("ids") String ids,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            @RequestParam Map<String, String> params,
            Model model,
            RedirectAttributes ra
    ) {

        // ===================== AUTH =====================
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            model.addAttribute("errorMessage", "Bạn chưa đăng nhập!");
            return paymentCart(ids, model);
        }

        Customer customer = customerService.findCustomerByUsername(auth.getName());
        if (customer == null) {
            model.addAttribute("errorMessage", "Không tìm thấy tài khoản khách hàng!");
            return paymentCart(ids, model);
        }

        // ===================== IDS =====================
        if (ids == null || ids.isBlank()) {
            model.addAttribute("errorMessage", "Giỏ hàng trống!");
            return "redirect:/home";
        }

        List<UUID> gameIds;
        try {
            gameIds = Arrays.stream(ids.split(","))
                    .filter(s -> !s.isBlank())
                    .map(UUID::fromString)
                    .toList();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "ID sản phẩm không hợp lệ!");
            return paymentCart(ids, model);
        }

        if (gameIds.isEmpty()) {
            model.addAttribute("errorMessage", "Không có sản phẩm hợp lệ!");
            return paymentCart(ids, model);
        }

        // ===================== LOAD GAME =====================
        List<GameAccount> games = gameIds.stream()
                .map(gameAccountService::findGameAccountById)
                .filter(Objects::nonNull)
                .toList();

        if (games.isEmpty()) {
            model.addAttribute("errorMessage", "Không tìm thấy tài khoản game!");
            return paymentCart(ids, model);
        }

        // ===================== PRICE CALC =====================
        BigDecimal totalAll = BigDecimal.ZERO;
        Map<UUID, Integer> durationMap = new HashMap<>();

        for (GameAccount g : games) {

            String pkgKey = "pkg_" + g.getId();
            String pkg = params.get(pkgKey);

            boolean isRent = g.getDuration() != null
                    && "RENT".equalsIgnoreCase(String.valueOf(g.getDuration()));

            // --- CHECK PACKAGE ---
            if (isRent) {
                if (pkg == null || pkg.isBlank()) {
                    model.addAttribute("errorMessage",
                            "Vui lòng chọn gói thuê cho tài khoản: " + g.getId());
                    return paymentCart(ids, model);
                }
            } else {
                pkg = "Vĩnh viễn";
            }

            BigDecimal price = g.getPrice();

            // --- DISCOUNT BY DURATION ---
            if (pkg.contains("2 Tháng")) {
                price = price.multiply(BigDecimal.valueOf(0.9));
            } else if (pkg.contains("3 Tháng")) {
                price = price.multiply(BigDecimal.valueOf(0.85));
            }

            price = price.setScale(0, RoundingMode.HALF_UP);
            totalAll = totalAll.add(price);

            int duration =
                    pkg.contains("1 Tháng") ? 1 :
                            pkg.contains("2 Tháng") ? 2 :
                                    pkg.contains("3 Tháng") ? 3 : 0;

            durationMap.put(g.getId(), duration);
        }

        // ===================== VOUCHER =====================
        Voucher voucher = null;
        if (voucherCode != null && !voucherCode.isBlank()) {

            voucher = voucherService.getValidVoucher(voucherCode);
            if (voucher == null) {
                model.addAttribute("errorMessage", "Voucher không hợp lệ hoặc đã hết hạn!");
                return paymentCart(ids, model);
            }

            if (voucherCustomerRepository.existsByCustomerAndVoucher(customer, voucher)) {
                model.addAttribute("errorMessage", "Voucher đã được sử dụng!");
                return paymentCart(ids, model);
            }

            BigDecimal discountPercent =
                    BigDecimal.valueOf(voucher.getValue()).divide(BigDecimal.valueOf(100));

            totalAll = totalAll.subtract(totalAll.multiply(discountPercent))
                    .setScale(0, RoundingMode.HALF_UP);
        }

        // ===================== BALANCE =====================
        if (customer.getBalance().compareTo(totalAll) < 0) {
            model.addAttribute("errorMessage", "Số dư không đủ!");
            return paymentCart(ids, model);
        }

        // ===================== DEDUCT MONEY =====================
        customer.setBalance(customer.getBalance().subtract(totalAll));
        customerRepositories.save(customer);

        // ===================== SAVE VOUCHER USED =====================
        if (voucher != null) {
            VoucherCustomer vc = new VoucherCustomer();
            vc.setCustomer(customer);
            vc.setVoucher(voucher);
            vc.setDateUsed(LocalDateTime.now());
            voucherCustomerRepository.save(vc);
        }

        // ===================== TRANSACTION =====================
        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(totalAll.negate());
        transaction.setDescription("Thanh toán giỏ hàng (" + games.size() + " tài khoản)");
        transaction.setDateCreated(LocalDateTime.now());
        transactionService.save(transaction);

        // ===================== ORDER =====================
        Orders order = new Orders();
        order.setCustomer(customer);
        order.setTotalPrice(totalAll);
        if (voucher != null) order.setVoucher(voucher);
        // staff = null, status = WAIT (auto)

        Orders savedOrder = ordersRepositories.save(order);

        // ===================== ORDER DETAIL =====================
        for (GameAccount g : games) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setGameAccount(g);
            detail.setDuration(durationMap.getOrDefault(g.getId(), 0));
            orderDetailRepositories.save(detail);
        }
        // ===================== CLEAN CART =====================
        deleteCartItemsOfCustomer(customer, gameIds);

        ra.addFlashAttribute("successPopup",
                "Thanh toán thành công (" + totalAll.toPlainString() + " đ)");

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
