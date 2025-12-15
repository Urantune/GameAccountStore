package webBackEnd.controller.Customer;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;
import webBackEnd.repository.CartRepositories;
import webBackEnd.repository.OrderDetailRepositories;
import webBackEnd.service.CartService;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameAccountService;

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
    private CartRepositories  cartRepositories;
    @Autowired
    private OrderDetailRepositories orderDetailRepositories;



    @GetMapping("/cart")
    public String showCart(Model model) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        Customer customer = customerService.findCustomerByUsername(username);

        model.addAttribute("list",
                cartService.getCartsByCustomer(customer));

        return "customer/cart";
    }


    @PostMapping("/add/{gameAccountId}")
    public ResponseEntity<String> addToCart(
            @PathVariable UUID gameAccountId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null)
            return ResponseEntity.status(401).body("Chưa đăng nhập");

        Customer customer =
                customerService.findCustomerByUsername(userDetails.getUsername());

        GameAccount gameAccount =
                gameAccountService.findGameAccountById(gameAccountId);

        if (gameAccount == null)
            return ResponseEntity.status(404).body("Không tìm thấy tài khoản");

        // 🔴 ĐÃ BÁN / ĐANG ĐẶT
        if (orderDetailRepositories.existsActiveOrderByGameAccount(gameAccountId)) {
            return ResponseEntity.badRequest()
                    .body("Tài khoản đã được đặt hoặc đã bán");
        }

        // 🔴 ĐÃ CÓ TRONG CART
        if (cartRepositories.existsByCustomerAndGameAccount(customer, gameAccount)) {
            return ResponseEntity.badRequest()
                    .body("Tài khoản đã có trong giỏ hàng");
        }

        cartService.addToCart(customer, gameAccount);
        return ResponseEntity.ok("Đã thêm vào giỏ hàng");
    }






}
