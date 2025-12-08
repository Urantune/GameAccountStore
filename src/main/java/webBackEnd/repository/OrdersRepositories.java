package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Orders;
@Repository
public interface OrdersRepositories extends JpaRepository<Orders,Integer> {
}
