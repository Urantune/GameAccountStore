package webBackEnd.service;

import jakarta.persistence.criteria.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.OrderDetail;
import webBackEnd.entity.Orders;
import webBackEnd.repository.OrderDetailRepositories;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderDetailService {

    @Autowired
    private OrderDetailRepositories orderDetailRepositories;
    @Autowired
    private OrdersService  ordersService;
    public List<GameAccount> getAllBoughtAccounts(UUID customerId) {
        return orderDetailRepositories.findAllGameAccountBoughtByCustomer(customerId);
    }


    public List<OrderDetail> findAllByOrderId(UUID orderId) {
        return orderDetailRepositories.findAllByOrder(ordersService.findById(orderId));
    }


    public List<OrderDetail> getOrderDetailByOrder(Orders o){
        return orderDetailRepositories.findByOrder(o);
    }

    public OrderDetail getFirstOrderDetailByOrder(Orders o) {
        List<OrderDetail> list = getOrderDetailByOrder(o);
        return list.isEmpty() ? null : list.get(0);
    }

    public OrderDetail findById(UUID id) {
        return orderDetailRepositories.findById(id).get();
    }

    public void delete(OrderDetail orderDetail) {
        orderDetailRepositories.delete(orderDetail);
    }


    public OrderDetail save(OrderDetail orderDetail) {
        return orderDetailRepositories.save(orderDetail);
    }


}

