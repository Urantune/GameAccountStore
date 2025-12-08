package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Cart;
@Repository
public interface CartRepositories extends JpaRepository<Cart,Integer> {
}
