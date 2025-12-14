package webBackEnd.service;

import org.springframework.stereotype.Service;
import webBackEnd.entity.Cart;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;
import webBackEnd.repository.CartRepositories;

import java.time.LocalDateTime;

@Service
public class CartService {

    private final CartRepositories cartRepositories;

    public CartService(CartRepositories cartRepositories) {
        this.cartRepositories = cartRepositories;
    }

    // ADD TO CART
    public void addToCart(Customer customer, GameAccount gameAccount) {

        // 1. Check trùng
        if (cartRepositories.existsByCustomerAndGameAccount(customer, gameAccount)) {
            throw new RuntimeException("Account đã tồn tại trong giỏ hàng");
        }

        // 2. Tạo cart mới
        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setGameAccount(gameAccount);
        cart.setCreatedAt(LocalDateTime.now());

        // 3. Lưu DB
        cartRepositories.save(cart);
    }
}
