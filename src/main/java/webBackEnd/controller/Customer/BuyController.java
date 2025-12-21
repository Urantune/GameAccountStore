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
    @Autowired
    private RentAccountGameService rentAccountGameService;


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
        rs.put("percent", v.getValue());

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
            @RequestParam(required = false) String voucherCode,
            @RequestParam UUID gameAccountId

    ) {
        GameAccount gameAccount = gameAccountService.findGameAccountById(gameAccountId);
        Game game = gameService.findById(gameId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Bạn chưa đăng nhập"));
        }
        Customer customer = customerService.findCustomerByUsername(auth.getName());
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Không tìm thấy khách hàng"));
        }
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Giá gốc không hợp lệ"));
        }
        boolean isRent = "rent".equalsIgnoreCase(accountType);
        if (isRent) {
            if (rentMonth < 1 || rentMonth > 3) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Chỉ được chọn thuê 1 - 3 tháng"));
            }
        } else {
            rentMonth = 0;
        }
        if ("permanent".equalsIgnoreCase(accountType)
                && rentAccountGameService.isAccountRented(gameAccount)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Tài khoản này đang được thuê, không thể mua vĩnh viễn"
                    ));
        }

        if (game == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không tìm thấy game"));
        }
        BigDecimal totalBeforeVoucher;
        if (!isRent) {
            totalBeforeVoucher = basePrice;
        } else {
            BigDecimal months = BigDecimal.valueOf(rentMonth);
            BigDecimal raw = basePrice.multiply(months);
            BigDecimal discountRate = BigDecimal.ONE; // 1.00
            if (rentMonth == 2) discountRate = new BigDecimal("0.90");
            if (rentMonth == 3) discountRate = new BigDecimal("0.85");
            totalBeforeVoucher = raw.multiply(discountRate);
        }
        totalBeforeVoucher = totalBeforeVoucher.setScale(0, RoundingMode.HALF_UP);
        Voucher usedVoucher = null;
        BigDecimal totalAfterVoucher = totalBeforeVoucher;
        if (voucherCode != null && !voucherCode.isBlank()) {
            usedVoucher = voucherService.getValidVoucher(voucherCode.trim());
            if (usedVoucher == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Voucher không hợp lệ hoặc đã hết hạn"));
            }
            if (voucherCustomerRepository.existsByCustomerAndVoucher(customer, usedVoucher)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Voucher đã được sử dụng"));
            }
            BigDecimal percent = BigDecimal.valueOf(usedVoucher.getValue()); // vd 10, 15...
            BigDecimal discount = totalBeforeVoucher.multiply(percent).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            totalAfterVoucher = totalBeforeVoucher.subtract(discount);
            if (totalAfterVoucher.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Giá sau voucher không hợp lệ"));
            }
        }
        if (customer.getBalance() == null || customer.getBalance().compareTo(totalAfterVoucher) < 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Số dư không đủ"));
        }
        boolean hasWaitingOrder =
                ordersRepositories.existsByCustomerAndStatus(customer, "WAIT");

        if (hasWaitingOrder) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Đơn hàng đang được xử lý. Vui lòng đợi admin duyệt đơn."
                    ));
        }

        customer.setBalance(customer.getBalance().subtract(totalAfterVoucher));
        customerRepositories.save(customer);
        Orders order = new Orders();
        order.setCustomer(customer);
        order.setTotalPrice(totalAfterVoucher);
        order.setStatus("WAIT");
        if (usedVoucher != null) {
            order.setVoucher(usedVoucher);
        }
        Orders savedOrder = ordersRepositories.save(order);

        OrderDetail detail = new OrderDetail();
        detail.setOrder(savedOrder);
        detail.setGame(game);
        detail.setGameAccount(gameAccount);
        if(rentMonth ==1){
            detail.setDuration(1);
        }else if(rentMonth ==2){
            detail.setDuration(2);
        }else if(rentMonth ==3){
            detail.setDuration(3);
        }
        else{
            detail.setDuration(0);
        }
        detail.setPrice(basePrice.setScale(0, RoundingMode.HALF_UP).intValue());
        detail.setLovel(gameAccount.getLovel());
        detail.setRank(gameAccount.getRank());
        detail.setSkin(gameAccount.getSkin());
        detail.setVip(gameAccount.getVip());

        orderDetailRepositories.save(detail);

        if(customer!=null && order.getTotalPrice()!=null){
            Transaction transaction = new Transaction();
            transaction.setCustomer(customer);
            transaction.setAmount(totalAfterVoucher.negate());
            transaction.setDescription("PAYMENT_COMLETED_ORDER"+ order.getId());
            transaction.setDateCreated(LocalDateTime.now());
            transactionService.save(transaction);
        }

        if (usedVoucher != null) {
            VoucherCustomer vc = new VoucherCustomer();
            vc.setCustomer(customer);
            vc.setVoucher(usedVoucher);
            vc.setDateUsed(LocalDateTime.now());
            voucherCustomerRepository.save(vc);
        }
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
