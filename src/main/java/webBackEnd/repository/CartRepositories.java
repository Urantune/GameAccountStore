package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Cart;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Game;
import webBackEnd.entity.GameAccount;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepositories extends JpaRepository<Cart, UUID> {



    Cart findByCartId(UUID cartId);


    List<Cart> findByCustomer(Customer customer);

//    @Query("""
//    SELECT COUNT(od) > 0
//    FROM OrderDetail od
//    JOIN od.order o
//    WHERE od.gameAccount.gameAccountId = :gameAccountId
//      AND o.status IN ('WAIT', 'COMPLETED')
//""")
//    boolean existsActiveOrderByGameAccount(UUID gameAccountId);



    List<Cart> findByCartIdInAndCustomer(
            List<UUID> cartIds,
            Customer customer
    );

    Optional<Cart> findTopByCustomerAndGameOrderByCreatedAtDesc(Customer customer, Game game);
    boolean existsByCustomerAndGameAccount(Customer customer, GameAccount gameAccount);

}
