package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Cart;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartRepositories extends JpaRepository<Cart, UUID> {

    boolean existsByCustomerAndGameAccount(Customer customer, GameAccount gameAccount);

    Cart findByCartId(UUID cartId);


    List<Cart> findByCustomer(Customer customer);


}
