package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Cart;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Game;
import webBackEnd.entity.GameAccount;
import webBackEnd.repository.CartRepositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CartService {

    @Autowired
    private CartRepositories cartRepositories;

    public void addToCart(Customer customer, GameAccount account, Integer duration,
                          String rank, int skin, int level, int vip) {
        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setGame(account.getGame()); // Lấy game từ account
        cart.setPrice(account.getPrice()); // Giá gốc của account
        cart.setDuration(duration);
        cart.setRank(rank);
        cart.setSkin(skin);
        cart.setLovel(level);
        cart.setVip(vip);
        cart.setCreatedAt(LocalDateTime.now());

        cartRepositories.save(cart);
    }





    public Cart getCartById(UUID id){
        return cartRepositories.findByCartId(id);
    }

    public List<Cart> getCartsByCustomer(Customer customer){
        return cartRepositories.findByCustomer(customer);
    }

    public void delete(Cart cart){
        cartRepositories.delete(cart);
    }

}
