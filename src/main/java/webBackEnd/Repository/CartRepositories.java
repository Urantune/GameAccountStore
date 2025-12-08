package webBackEnd.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.Entity.Cart;
@Repository
public interface CartRepositories extends JpaRepository<Cart,Integer> {
}
