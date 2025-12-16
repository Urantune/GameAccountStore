package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Customer;
import webBackEnd.entity.OrderDetail;
import webBackEnd.entity.Orders;
import webBackEnd.repository.OrdersRepositories;

import java.util.List;
import java.util.UUID;

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

    public Orders findByStatus(String status){
        return ordersRepositories.findByStatus(status);
    }

    public Orders findById(UUID id) {
        return ordersRepositories.findById(id).get();
    }

    public Orders save(Orders orders) {
        return ordersRepositories.save(orders);
    }

    public List<Orders> findByCusOrdersId(String status,  Customer customer) {
        return ordersRepositories.findAllByStatusAndCustomer(status,customer);
    }

    public void delete(Orders orders) {
        ordersRepositories.delete(orders);
    }






}
