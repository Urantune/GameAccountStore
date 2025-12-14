package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Cart;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;

import java.util.UUID;

@Repository
public interface CartRepositories extends JpaRepository<Cart, UUID> {
    // kiểm tra account đã có trong giỏ của user chưa
    boolean existsByCustomerAndGameAccount(Customer customer, GameAccount gameAccount);
}
