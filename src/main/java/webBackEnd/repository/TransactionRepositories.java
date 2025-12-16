package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Transaction;


import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepositories extends JpaRepository<Transaction,UUID> {

    // Lấy lịch sử theo customer
    List<Transaction> findByCustomerOrderByDateCreatedDesc(Customer customer);

    // Search theo mô tả
    List<Transaction>
    findByCustomerAndDescriptionContainingIgnoreCaseOrderByDateCreatedDesc(
            Customer customer, String keyword
    );

}