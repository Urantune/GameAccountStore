package webBackEnd.controller.Customer;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;
import webBackEnd.service.CartService;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameAccountService;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CustomerService customerService;
    private final GameAccountService gameAccountService;

    public CartController(CartService cartService,
                          CustomerService customerService,
                          GameAccountService gameAccountService) {
        this.cartService = cartService;
        this.customerService = customerService;
        this.gameAccountService = gameAccountService;
    }

    @PostMapping("/add/{gameAccountId}")
    public ResponseEntity<?> addToCart(@PathVariable UUID gameAccountId,
                                       @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập");
        }

        Customer customer =
                customerService.findByCustomerUsername(userDetails.getUsername());

        GameAccount gameAccount =
                gameAccountService.findGameAccountById(gameAccountId);

        cartService.addToCart(customer, gameAccount);

        return ResponseEntity.ok("Đã thêm vào giỏ hàng");
    }
}
