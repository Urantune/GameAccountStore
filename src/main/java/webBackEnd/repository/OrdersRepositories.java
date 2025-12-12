package webBackEnd.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Orders;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrdersRepositories extends JpaRepository<Orders, UUID> {

    List<Orders> findAllByStatus(String status);

    Orders findByStatus(String status);



}
