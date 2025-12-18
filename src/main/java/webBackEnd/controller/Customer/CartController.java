package webBackEnd.controller.Customer;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.*;
import webBackEnd.repository.*;
import webBackEnd.service.CartService;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameService;
import webBackEnd.service.TransactionService;

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

    // ✅ thêm để lưu order
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

    @PostMapping("/cart/add/{gameId}")
    public ResponseEntity<String> addToCart(
            @PathVariable UUID gameId,
            @RequestParam BigDecimal basePrice,
            @RequestParam(defaultValue = "0") Integer duration,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

        Customer customer = customerService.findCustomerByUsername(userDetails.getUsername());
        Game game = gameService.findById(gameId);

        if (game == null) return ResponseEntity.status(404).body("Không tìm thấy game");

        cartService.addToCart(customer, game, basePrice, duration);
        return ResponseEntity.ok("Đã thêm vào giỏ hàng");
    }

    @PostMapping("/cart/delete/{id}")
    public String deleteCart(@PathVariable("id") UUID cartId, RedirectAttributes ra) {
        cartService.delete(cartService.getCartById(cartId));
        ra.addFlashAttribute("successPopup", "Đã xoá khỏi giỏ hàng");
        return "redirect:/home/cart";
    }


    @PostMapping("/cart/checkout")
    @Transactional
    public String checkoutFromCart(
            @RequestParam(value = "selectedCartIds", required = false) List<UUID> selectedCartIds,
            RedirectAttributes ra
    ) {
        if (selectedCartIds == null || selectedCartIds.isEmpty()) {
            ra.addFlashAttribute("errorPopup", "Vui lòng tick ít nhất 1 món để thanh toán");
            return "redirect:/home/cart";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            ra.addFlashAttribute("errorPopup", "Bạn chưa đăng nhập");
            return "redirect:/login";
        }

        Customer customer = customerService.findCustomerByUsername(auth.getName());
        if (customer == null) {
            ra.addFlashAttribute("errorPopup", "Không tìm thấy khách hàng");
            return "redirect:/home/cart";
        }

        List<Cart> carts = selectedCartIds.stream()
                .map(cartService::getCartById) // hoặc cartRepositories.findById(id).orElse(null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

         UUID cusId = customer.getCustomerId();
        carts = carts.stream()
                .filter(c -> c.getCustomer() != null
                        && c.getCustomer().getCustomerId() != null
                        && c.getCustomer().getCustomerId().equals(cusId))
                .collect(Collectors.toList());

        if (carts.isEmpty()) {
            ra.addFlashAttribute("errorPopup", "Giỏ hàng không hợp lệ");
            return "redirect:/home/cart";
        }

       BigDecimal total = carts.stream()
                .map(c -> c.getPrice() != null ? c.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("errorPopup", "Tổng tiền không hợp lệ");
            return "redirect:/home/cart";
        }

        if (customer.getBalance() == null || customer.getBalance().compareTo(total) < 0) {
            ra.addFlashAttribute("errorPopup", "Số dư không đủ");
            return "redirect:/home/wallet";
        }

       customer.setBalance(customer.getBalance().subtract(total));
        customerRepositories.save(customer);

        Orders order = new Orders();
        order.setCustomer(customer);
        order.setTotalPrice(total);
        order.setStatus("WAIT");

        Orders savedOrder = ordersRepositories.save(order);

         for (Cart c : carts) {
            OrderDetail d = new OrderDetail();
            d.setOrder(savedOrder);
            d.setGame(c.getGame());
            d.setGameAccount(null);
            d.setDuration(c.getDuration() == null ? 0 : c.getDuration());


            BigDecimal p = c.getPrice() != null ? c.getPrice() : BigDecimal.ZERO;
            d.setPrice(p.intValue());

            orderDetailRepositories.save(d);
        }


        for (Cart c : carts) {
            cartService.delete(c);
        }




        ra.addFlashAttribute("successPopup", "Thanh toán giỏ hàng thành công! Vui lòng chờ admin xử lý.");
        return "redirect:/home";
    }
}
