package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;
import webBackEnd.repository.CartRepositories;
import webBackEnd.repository.OrderDetailRepositories;
import webBackEnd.service.CartService;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameAccountService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/home")
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private GameAccountService gameAccountService;
    @Autowired
    private CartRepositories cartRepositories;
    @Autowired
    private OrderDetailRepositories orderDetailRepositories;

    @GetMapping("/cart")
    public String showCart(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Customer customer = customerService.findCustomerByUsername(username);
        var carts = cartService.getCartsByCustomer(customer);

        BigDecimal totalAll = carts.stream()
                .map(c -> c.getGameAccount() != null && c.getGameAccount().getPrice() != null ? c.getGameAccount().getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("list", carts);
        model.addAttribute("totalAll", totalAll);
        return "customer/cart";
    }

    @PostMapping("/add/{gameAccountId}")
    public ResponseEntity<String> addToCart(
            @PathVariable UUID gameAccountId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

        Customer customer = customerService.findCustomerByUsername(userDetails.getUsername());
        GameAccount gameAccount = gameAccountService.findGameAccountById(gameAccountId);

        if (gameAccount == null) return ResponseEntity.status(404).body("Không tìm thấy tài khoản");

        if (orderDetailRepositories.existsActiveOrderByGameAccount(gameAccountId)) {
            return ResponseEntity.badRequest().body("Tài khoản đã được đặt hoặc đã bán");
        }

        if (cartRepositories.existsByCustomerAndGameAccount(customer, gameAccount)) {
            return ResponseEntity.badRequest().body("Tài khoản đã có trong giỏ hàng");
        }

        cartService.addToCart(customer, gameAccount);
        return ResponseEntity.ok("Đã thêm vào giỏ hàng");
    }

    @PostMapping("/cart/delete/{id}")
    public String deleteCart(@PathVariable("id") UUID cartId, RedirectAttributes ra) {
        cartService.delete(cartService.getCartById(cartId));
        ra.addFlashAttribute("successPopup", "Đã xoá khỏi giỏ hàng");
        return "redirect:/home/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkoutFromCart(
            @RequestParam(value = "selectedGameIds", required = false) List<UUID> selectedGameIds,
            RedirectAttributes ra
    ) {
        if (selectedGameIds == null || selectedGameIds.isEmpty()) {
            ra.addFlashAttribute("errorPopup", "Vui lòng tick ít nhất 1 món để thanh toán");
            return "redirect:/home/cart";
        }

        if (selectedGameIds.size() == 1) {
            return "redirect:/home/payment/" + selectedGameIds.get(0);
        }

        String ids = selectedGameIds.stream().map(UUID::toString).reduce((a, b) -> a + "," + b).orElse("");
        return "redirect:/home/payment/cart?ids=" + ids;
    }
}
