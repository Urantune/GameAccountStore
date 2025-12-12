package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Orders;
import webBackEnd.repository.OrdersRepositories;

import java.util.List;

@Service
public class OrdersService {


    @Autowired
    private OrdersRepositories  ordersRepositories;

    public List<Orders> findAll(){
        return ordersRepositories.findAll();


    }

    public List<Orders> findAllByStatus(String status){
        return ordersRepositories.findAllByStatus(status);
    }


}
