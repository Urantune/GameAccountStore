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

    @GetMapping("/payment/{id}")
    public String checkout(@PathVariable("id") UUID id, Model model) {
        GameAccount game = gameAccountService.findGameAccountById(id);
        model.addAttribute("games", game);
        return "customer/Payment";
    }

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

    @Transactional
    @PostMapping("/order/confirmCart")
    public String confirmCart(
            @RequestParam("ids") String ids,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            @RequestParam Map<String, String> params,
            Model model,
            RedirectAttributes ra
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            model.addAttribute("errorMessage", "Bạn chưa đăng nhập!");
            return paymentCart(ids, model);
        }

        Customer customer = customerService.findCustomerByUsername(authentication.getName());

        var gameIds = Arrays.stream(ids.split(","))
                .filter(s -> !s.isBlank())
                .map(UUID::fromString)
                .toList();

        var games = gameIds.stream()
                .map(gameAccountService::findGameAccountById)
                .filter(Objects::nonNull)
                .toList();

        if (games.isEmpty()) {
            model.addAttribute("errorMessage", "Không có món nào hợp lệ để thanh toán!");
            return paymentCart(ids, model);
        }

        for (GameAccount g : games) {
            boolean isOrdered = orderDetailRepositories.existsActiveOrderByGameAccount(g.getId());
            if (isOrdered) {
                model.addAttribute("errorMessage", "Có tài khoản đã được đặt/bán rồi: " + g.getId());
                return paymentCart(ids, model);
            }
        }

        BigDecimal totalAll = BigDecimal.ZERO;
        Map<UUID, Integer> durationMap = new HashMap<>();

        for (GameAccount g : games) {
            String pkgKey = "pkg_" + g.getId();
            String pkg = params.get(pkgKey);

            boolean isRent = (g.getDuration() != null && "RENT".equalsIgnoreCase(String.valueOf(g.getDuration())));
            if (isRent) {
                if (pkg == null || pkg.isBlank()) {
                    model.addAttribute("errorMessage", "Vui lòng chọn gói thuê cho tài khoản: " + g.getId());
                    return paymentCart(ids, model);
                }
            } else {
                if (pkg == null || pkg.isBlank()) pkg = "Vĩnh viễn";
            }

            BigDecimal price = g.getPrice();

            if (pkg.contains("2 Tháng")) {
                price = price.multiply(BigDecimal.valueOf(0.9));
            } else if (pkg.contains("3 Tháng")) {
                price = price.multiply(BigDecimal.valueOf(0.85));
            }

            price = price.setScale(0, RoundingMode.HALF_UP);
            totalAll = totalAll.add(price);

            int dur =
                    pkg.contains("1 Tháng") ? 1 :
                            pkg.contains("2 Tháng") ? 2 :
                                    pkg.contains("3 Tháng") ? 3 : 0;

            durationMap.put(g.getId(), dur);
        }

        Voucher voucher = null;
        if (voucherCode != null && !voucherCode.isBlank()) {
            voucher = voucherService.getValidVoucher(voucherCode);
            if (voucher == null) {
                model.addAttribute("errorMessage", "Voucher không hợp lệ hoặc đã hết hạn");
                return paymentCart(ids, model);
            }

            boolean used = voucherCustomerRepository.existsByCustomerAndVoucher(customer, voucher);
            if (used) {
                model.addAttribute("errorMessage", "Voucher này đã được sử dụng trước đó");
                return paymentCart(ids, model);
            }

            BigDecimal discountPercent = BigDecimal.valueOf(voucher.getValue()).divide(BigDecimal.valueOf(100));
            totalAll = totalAll.subtract(totalAll.multiply(discountPercent));
            totalAll = totalAll.setScale(0, RoundingMode.HALF_UP);
        }

        if (customer.getBalance().compareTo(totalAll) < 0) {
            model.addAttribute("errorMessage", "Số dư không đủ");
            return paymentCart(ids, model);
        }

        customer.setBalance(customer.getBalance().subtract(totalAll));
        customerRepositories.save(customer);

        if (voucher != null) {
            VoucherCustomer vc = new VoucherCustomer();
            vc.setCustomer(customer);
            vc.setVoucher(voucher);
            vc.setDateUsed(LocalDateTime.now());
            voucherCustomerRepository.save(vc);
        }

        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(totalAll.negate());
        transaction.setDescription("Thanh toán giỏ hàng (" + games.size() + " món)");
        transaction.setDateCreated(LocalDateTime.now());
        transactionService.save(transaction);

        Orders order = new Orders();
        order.setCustomer(customer);
        order.setTotalPrice(totalAll);
        order.setCreatedDate(LocalDateTime.now());
        order.setStatus("WAIT");
        if (voucher != null) order.setVoucher(voucher);

        Orders savedOrder = ordersRepositories.save(order);

        for (GameAccount g : games) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setGameAccount(g);
            detail.setDuration(durationMap.getOrDefault(g.getId(), 0));
            orderDetailRepositories.save(detail);
        }

        deleteCartItemsOfCustomer(customer, gameIds);

        ra.addFlashAttribute("successPopup", "Thanh toán giỏ hàng thành công (" + totalAll.toPlainString() + " đ)");
        return "redirect:/home";
    }

    @Transactional
    @PostMapping("/order/confirm/{gameId}")
    public String confirmOrder(
            @PathVariable UUID gameId,
            @RequestParam("packageValues") String packageValues,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            Model model,
            RedirectAttributes ra
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            model.addAttribute("errorMessage", "Bạn chưa đăng nhập!");
            model.addAttribute("games", getGame(gameId));
            return "customer/Payment";
        }

        Customer customer = customerService.findCustomerByUsername(authentication.getName());
        GameAccount game = getGame(gameId);

        boolean isOrdered = orderDetailRepositories.existsActiveOrderByGameAccount(game.getId());
        if (isOrdered) {
            model.addAttribute("errorMessage", "Tài khoản này đã được đặt hoặc đã bán, không thể tiếp tục đặt hàng!");
            model.addAttribute("games", game);
            return "customer/Payment";
        }

        boolean isRent = (game.getDuration() != null && "RENT".equalsIgnoreCase(String.valueOf(game.getDuration())));
        if (isRent && (packageValues == null || packageValues.isBlank())) {
            model.addAttribute("errorMessage", "Vui lòng chọn gói thuê");
            model.addAttribute("games", game);
            return "customer/Payment";
        }
        if (!isRent && (packageValues == null || packageValues.isBlank())) {
            packageValues = "Vĩnh viễn";
        }

        BigDecimal totalPrice = game.getPrice();

        if (packageValues.contains("2 Tháng")) {
            totalPrice = totalPrice.multiply(BigDecimal.valueOf(0.9));
        } else if (packageValues.contains("3 Tháng")) {
            totalPrice = totalPrice.multiply(BigDecimal.valueOf(0.85));
        }

        Voucher voucher = null;
        if (voucherCode != null && !voucherCode.isBlank()) {
            voucher = voucherService.getValidVoucher(voucherCode);
            if (voucher == null) {
                model.addAttribute("errorMessage", "Voucher không hợp lệ hoặc đã hết hạn");
                model.addAttribute("games", game);
                return "customer/Payment";
            }

            boolean used = voucherCustomerRepository.existsByCustomerAndVoucher(customer, voucher);
            if (used) {
                model.addAttribute("errorMessage", "Voucher này đã được sử dụng trước đó");
                model.addAttribute("games", game);
                return "customer/Payment";
            }

            BigDecimal discountPercent = BigDecimal.valueOf(voucher.getValue()).divide(BigDecimal.valueOf(100));
            totalPrice = totalPrice.subtract(totalPrice.multiply(discountPercent));
        }

        totalPrice = totalPrice.setScale(0, RoundingMode.HALF_UP);

        if (customer.getBalance().compareTo(totalPrice) < 0) {
            model.addAttribute("errorMessage", "Số dư không đủ");
            model.addAttribute("games", game);
            return "customer/Payment";
        }

        customer.setBalance(customer.getBalance().subtract(totalPrice));
        customerRepositories.save(customer);

        if (voucher != null) {
            VoucherCustomer vc = new VoucherCustomer();
            vc.setCustomer(customer);
            vc.setVoucher(voucher);
            vc.setDateUsed(LocalDateTime.now());
            voucherCustomerRepository.save(vc);
        }

        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(totalPrice.negate());
        transaction.setDescription("Thanh toán game ID: " + gameId);
        transaction.setDateCreated(LocalDateTime.now());
        transactionService.save(transaction);

        Orders order = new Orders();
        order.setCustomer(customer);
        order.setTotalPrice(totalPrice);
        order.setCreatedDate(LocalDateTime.now());
        order.setStatus("WAIT");
        if (voucher != null) order.setVoucher(voucher);

        Orders savedOrder = ordersRepositories.save(order);

        OrderDetail detail = new OrderDetail();
        detail.setOrder(savedOrder);
        detail.setGameAccount(game);
        detail.setDuration(
                packageValues.contains("1 Tháng") ? 1 :
                        packageValues.contains("2 Tháng") ? 2 :
                                packageValues.contains("3 Tháng") ? 3 : 0
        );
        orderDetailRepositories.save(detail);

        deleteCartItemsOfCustomer(customer, List.of(gameId));

        ra.addFlashAttribute("successPopup", "Thanh toán thành công (" + totalPrice.toPlainString() + " đ)");
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
