package webBackEnd.controller.Customer;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/home")
public class CartController {
    @Autowired
    private VoucherCustomerRepository voucherCustomerRepository;
    @Autowired
    private VoucherService voucherService;
    @Autowired
    private RentAccountGameService rentAccountGameService;
    @Autowired
    private CartService cartService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private GameService gameService;
    @Autowired
    private CartRepositories cartRepositories;
    @Autowired
    private GameAccountService gameAccountService;
    @Autowired
    private OrdersRepositories ordersRepositories;
    @Autowired
    private OrderDetailRepositories orderDetailRepositories;
    @Autowired
    private CustomerRepositories customerRepositories;

    @Autowired
    private TransactionService transactionService;

    @ModelAttribute("currentUser")
    public Customer currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerService.findCustomerByUsername(username);
    }

    @GetMapping("/cart")
    public String showCart(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Customer customer = customerService.findCustomerByUsername(username);
        var carts = cartService.getCartsByCustomer(customer);

        BigDecimal totalAll = carts.stream()
                .map(c -> c.getPrice() != null ? c.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("list", carts);
        model.addAttribute("totalAll", totalAll);
        return "customer/cart";
    }

    @PostMapping("/cart/add/{accountId}")
    public ResponseEntity<String> addToCart(
            @PathVariable("accountId") String accountIdStr,
            @RequestParam(defaultValue = "0") Integer duration,
            @RequestParam int skin,
            @RequestParam int level,
            @RequestParam int vip,
            @RequestParam String rank,
            @RequestParam(defaultValue = "0") int voucherPercent,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");

        if (voucherPercent > 0)
            return ResponseEntity.badRequest().body("Không thể thêm vào giỏ hàng khi áp voucher");

        UUID accountId;
        try {
            accountId = UUID.fromString(accountIdStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Account ID không hợp lệ");
        }

        Customer customer = customerService.findCustomerByUsername(userDetails.getUsername());
        GameAccount account = gameAccountService.findGameAccountById(accountId);

        if (account == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy account");

        boolean exists =
                cartRepositories.existsByCustomerAndGameAccount(customer, account);

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Tài khoản này đã có trong giỏ hàng");
        }


        List<Cart> carts = cartRepositories.findByCustomer(customer);
        if (carts.size() >= 5)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Số lượng đơn hàng trong cart đã đạt tối đa");

        cartService.addToCart(customer, account, duration, rank, skin, level, vip);

        return ResponseEntity.ok("Đã thêm vào giỏ hàng");
    }

    @PostMapping("/cart/delete/{id}")
    public String deleteCart(@PathVariable UUID id,
                             RedirectAttributes ra) {
        cartService.deleteById(id);
        ra.addFlashAttribute("successPopup", "Đã xoá khỏi giỏ hàng");

        return "redirect:/home/cart";
    }


    @PostMapping("/cart/update-duration")
    @ResponseBody
    @Transactional
    public Map<String, Object> updateCartDuration(
            @RequestParam UUID cartId,
            @RequestParam int duration,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return Map.of("success", false, "message", "Chưa đăng nhập");
        }

        if (duration < 1 || duration > 3) {
            return Map.of("success", false, "message", "Gói thuê không hợp lệ");
        }

        Cart cart = cartRepositories.findById(cartId).orElse(null);
        if (cart == null) {
            return Map.of("success", false, "message", "Không tìm thấy cart");
        }

        if (cart.getDuration() == 0) {
            return Map.of("success", false, "message", "Không thể đổi gói vĩnh viễn");
        }

        cart.setDuration(duration);
        cartRepositories.save(cart);

        return Map.of("success", true);
    }


    @PostMapping("/cart/checkout")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> checkoutCart(
            @RequestParam String cartIds,
            @RequestParam(required = false) String voucherCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Bạn chưa đăng nhập"));
        }

        Customer customer =
                customerService.findCustomerByUsername(userDetails.getUsername());

        List<UUID> cartIdList = Arrays.stream(cartIds.split(","))
                .map(UUID::fromString)
                .toList();

        List<Cart> carts =
                cartRepositories.findByCartIdInAndCustomer(cartIdList, customer);


        if (carts.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Giỏ hàng trống"));
        }

        if (voucherCode != null && !voucherCode.isBlank() && carts.size() > 1) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Voucher chỉ áp dụng khi thanh toán 1 account"
                    ));
        }

        BigDecimal totalBeforeVoucher = BigDecimal.ZERO;

        for (Cart c : carts) {
            GameAccount acc = c.getGameAccount();

            if (c.getDuration() == 0 &&
                    rentAccountGameService.isAccountRented(acc)) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message",
                                "Có account đang được thuê, không thể mua vĩnh viễn"
                        ));
            }

            BigDecimal price = c.getPrice();
            BigDecimal itemTotal;

            if (c.getDuration() == 0) {
                itemTotal = price;
            } else {
                BigDecimal months = BigDecimal.valueOf(c.getDuration());
                BigDecimal raw = price.multiply(months);
                BigDecimal rate = BigDecimal.ONE;

                if (c.getDuration() == 2) rate = new BigDecimal("0.90");
                if (c.getDuration() == 3) rate = new BigDecimal("0.85");

                itemTotal = raw.multiply(rate);
            }

            totalBeforeVoucher =
                    totalBeforeVoucher.add(itemTotal);
        }

        totalBeforeVoucher =
                totalBeforeVoucher.setScale(0, RoundingMode.HALF_UP);

        Voucher usedVoucher = null;
        BigDecimal totalAfterVoucher = totalBeforeVoucher;

        if (voucherCode != null && !voucherCode.isBlank()) {
            usedVoucher = voucherService.getValidVoucher(voucherCode.trim());

            if (usedVoucher == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Voucher không hợp lệ"));
            }

            if (voucherCustomerRepository
                    .existsByCustomerAndVoucher(customer, usedVoucher)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Voucher đã được sử dụng"));
            }

            BigDecimal percent =
                    BigDecimal.valueOf(usedVoucher.getValue());

            BigDecimal discount =
                    totalBeforeVoucher.multiply(percent)
                            .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);

            totalAfterVoucher = totalBeforeVoucher.subtract(discount);
        }

        if (customer.getBalance().compareTo(totalAfterVoucher) < 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Số dư không đủ"));
        }
        customer.setBalance(
                customer.getBalance().subtract(totalAfterVoucher));
        customerRepositories.save(customer);

        Orders order = new Orders();
        order.setCustomer(customer);
        order.setTotalPrice(totalAfterVoucher);
        order.setStatus("WAIT");

        if (usedVoucher != null)
            order.setVoucher(usedVoucher);

        Orders savedOrder = ordersRepositories.save(order);

        if (customer != null && order.getTotalPrice() != null) {
            Transaction transaction = new Transaction();
            transaction.setCustomer(customer);
            transaction.setAmount(totalAfterVoucher.negate());
            transaction.setDescription("PAYMENT_COMLETED_ORDER" + savedOrder.getId());
            transaction.setDateCreated(LocalDateTime.now());
            transactionService.save(transaction);
        }


        for (Cart c : carts) {
            OrderDetail d = new OrderDetail();
            d.setOrder(savedOrder);
            d.setGame(c.getGame());
            d.setGameAccount(c.getGameAccount());
            d.setDuration(c.getDuration());
            d.setPrice(c.getPrice().intValue());
            d.setRank(c.getRank());
            d.setSkin(c.getSkin());
            d.setLovel(c.getLovel());
            d.setVip(c.getVip());

            orderDetailRepositories.save(d);
        }
        if (usedVoucher != null) {
            VoucherCustomer vc = new VoucherCustomer();
            vc.setCustomer(customer);
            vc.setVoucher(usedVoucher);
            vc.setDateUsed(LocalDateTime.now());
            voucherCustomerRepository.save(vc);
        }
        cartRepositories.deleteAll(carts);
        return ResponseEntity.ok(
                Map.of("success", true, "message", "Thanh toán giỏ hàng thành công")
        );
    }
}
