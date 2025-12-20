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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/home")
public class CartController {

    @Autowired private CartService cartService;
    @Autowired private CustomerService customerService;
    @Autowired private GameService gameService;
    @Autowired private CartRepositories cartRepositories;
@Autowired
private GameAccountService gameAccountService;
    @Autowired private OrdersRepositories ordersRepositories;
    @Autowired private OrderDetailRepositories orderDetailRepositories;
    @Autowired private CustomerRepositories customerRepositories;

    @Autowired
    private TransactionService transactionService;

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
            @RequestParam(defaultValue = "0") int voucherPercent, // voucher đã bị chặn ở JS
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

        List<Cart> carts = cartRepositories.findByCustomer(customer);
        if (carts.size() >= 5)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Số lượng đơn hàng trong cart đã đạt tối đa");

        cartService.addToCart(customer, account, duration, rank, skin, level, vip);

        return ResponseEntity.ok("Đã thêm vào giỏ hàng");
    }
    @PostMapping("/cart/delete/{id}")
    public String deleteCart(@PathVariable("id") UUID cartId, RedirectAttributes ra) {
        cartService.delete(cartService.getCartById(cartId));
        ra.addFlashAttribute("successPopup", "Đã xoá khỏi giỏ hàng");
        return "redirect:/home/cart";
    }


    @PostMapping("/cart/checkout")
    @ResponseBody
    @Transactional
    public Map<String, Object> checkoutFromCart(
            @RequestParam(value = "selectedCartIds", required = false) List<UUID> selectedCartIds
    ) {
        Map<String, Object> res = new HashMap<>();

        if (selectedCartIds == null || selectedCartIds.isEmpty()) {
            res.put("success", false);
            res.put("message", "Vui lòng tick ít nhất 1 món để thanh toán");
            return res;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            res.put("success", false);
            res.put("message", "Bạn chưa đăng nhập");
            return res;
        }

        Customer customer = customerService.findCustomerByUsername(auth.getName());
        if (customer == null) {
            res.put("success", false);
            res.put("message", "Không tìm thấy khách hàng");
            return res;
        }

        List<Cart> carts = selectedCartIds.stream()
                .map(cartService::getCartById)
                .filter(Objects::nonNull)
                .filter(c -> c.getCustomer() != null
                        && c.getCustomer().getCustomerId().equals(customer.getCustomerId()))
                .toList();

        if (carts.isEmpty()) {
            res.put("success", false);
            res.put("message", "Giỏ hàng không hợp lệ");
            return res;
        }


        BigDecimal total = BigDecimal.ZERO;

        for (Cart c : carts) {
            BigDecimal basePrice = (c.getPrice() != null) ? c.getPrice() : BigDecimal.ZERO;
            if (basePrice.compareTo(BigDecimal.ZERO) <= 0) {
                res.put("success", false);
                res.put("message", "Có sản phẩm trong giỏ có giá không hợp lệ");
                return res;
            }

            int duration = (c.getDuration() == null) ? 0 : c.getDuration();
            if (duration < 0 || duration > 3) {
                res.put("success", false);
                res.put("message", "Có sản phẩm có duration không hợp lệ (chỉ 0-3)");
                return res;
            }

            BigDecimal itemTotal;

            if (duration == 0) {

                itemTotal = basePrice;
            } else {

                BigDecimal months = BigDecimal.valueOf(duration);
                BigDecimal raw = basePrice.multiply(months);

                BigDecimal rate = BigDecimal.ONE;       // 1.00
                if (duration == 2) rate = new BigDecimal("0.90");
                if (duration == 3) rate = new BigDecimal("0.85");

                itemTotal = raw.multiply(rate);
            }

            itemTotal = itemTotal.setScale(0, java.math.RoundingMode.HALF_UP);
            total = total.add(itemTotal);
        }

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            res.put("success", false);
            res.put("message", "Tổng tiền không hợp lệ");
            return res;
        }

        if (customer.getBalance() == null || customer.getBalance().compareTo(total) < 0) {
            res.put("success", false);
            res.put("message", "Số dư không đủ");
            return res;
        }


        customer.setBalance(customer.getBalance().subtract(total));
        customerRepositories.save(customer);

        Orders order = new Orders();
        order.setCustomer(customer);
        order.setTotalPrice(total);
        order.setStatus("WAIT");
        Orders savedOrder = ordersRepositories.save(order);


        for (Cart c : carts) {
            BigDecimal basePrice = (c.getPrice() != null) ? c.getPrice() : BigDecimal.ZERO;
            int duration = (c.getDuration() == null) ? 0 : c.getDuration();

            OrderDetail d = new OrderDetail();
            d.setOrder(savedOrder);
            d.setGame(c.getGame());
            d.setDuration(duration);
            d.setPrice(basePrice.setScale(0, java.math.RoundingMode.HALF_UP).intValue());
            orderDetailRepositories.save(d);
        }

        carts.forEach(cartService::delete);

        res.put("success", true);
        res.put("message", "Thanh toán giỏ hàng thành công! Vui lòng chờ admin xử lý.");
        return res;
    }


}
