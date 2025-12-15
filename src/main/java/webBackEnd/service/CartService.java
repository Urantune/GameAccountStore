package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Cart;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;
import webBackEnd.repository.CartRepositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CartService {

    @Autowired
    private CartRepositories cartRepositories;

    public void addToCart(Customer customer, GameAccount gameAccount) {


        if (cartRepositories.existsByCustomerAndGameAccount(customer, gameAccount)) {
            throw new RuntimeException("Account đã tồn tại trong giỏ hàng");
        }


        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setGameAccount(gameAccount);
        cart.setCreatedAt(LocalDateTime.now());


        cartRepositories.save(cart);
    }


    public Cart getCartById(UUID id){
        return cartRepositories.findByCartId(id);
    }

    public List<Cart> getCartsByCustomer(Customer customer){
        return cartRepositories.findByCustomer(customer);
    }
}
