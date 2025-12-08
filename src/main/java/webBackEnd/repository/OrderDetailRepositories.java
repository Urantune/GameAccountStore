package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.OrderDetail;

@Repository
public interface OrderDetailRepositories extends JpaRepository<OrderDetail,Integer> {
}
