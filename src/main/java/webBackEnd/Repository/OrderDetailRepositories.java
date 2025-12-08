package webBackEnd.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.Entity.OrderDetail;

@Repository
public interface OrderDetailRepositories extends JpaRepository<OrderDetail,Integer> {
}
