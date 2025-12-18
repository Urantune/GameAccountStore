package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Transaction;


import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepositories extends JpaRepository<Transaction,UUID> {

    List<Transaction> findByCustomerOrderByDateCreatedDesc(Customer customer);

    List<Transaction>
    findByCustomerAndDescriptionContainingIgnoreCaseOrderByDateCreatedDesc(
            Customer customer, String keyword
    );

    List<Transaction> findByCustomerAndTransactionId(
            Customer customer, UUID transactionId);

    List<Transaction> findByCustomer_CustomerId(UUID customerId);

    List<Transaction> findByCustomer(Customer customer);


}
