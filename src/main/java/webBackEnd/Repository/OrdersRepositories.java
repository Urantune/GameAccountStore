package webBackEnd.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.Entity.Orders;
@Repository
public interface OrdersRepositories extends JpaRepository<Orders,Integer> {
}
