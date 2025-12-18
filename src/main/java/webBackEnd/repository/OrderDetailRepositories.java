package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.OrderDetail;
import webBackEnd.entity.Orders;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderDetailRepositories extends JpaRepository<OrderDetail, UUID> {
    @Query("SELECT od.gameAccount FROM OrderDetail od JOIN od.order o WHERE o.customer.customerId = :customerId")
    List<GameAccount> findAllGameAccountBoughtByCustomer(UUID customerId);

    List<OrderDetail> findAllByOrder(Orders order);



    @Query("""
                select count(od) > 0
                from OrderDetail od
                join od.order o
                where od.gameAccount.gameAccountId = :gameAccountId
                  and o.status in ('WAIT', 'COMPLETED')
            """)
    boolean existsActiveOrderByGameAccount(@Param("gameAccountId") UUID gameAccountId);

    List<OrderDetail> findByOrder(Orders order);

    List<OrderDetail> findAllByGameAccount(GameAccount gameAccount);

    long countByOrder(Orders order);

    List<OrderDetail> findAllByOrder_Id(UUID orderId);

}
