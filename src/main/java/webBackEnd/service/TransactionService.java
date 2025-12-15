package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Transaction;
import webBackEnd.repository.TransactionRepositories;

import java.util.List;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepositories transactionRepositories;

    public List<Transaction> getAll() {
        return transactionRepositories.findAll();
    }

    // LỊCH SỬ GIAO DỊCH
    public List<Transaction> getTransactionHistory(Customer customer) {
        return transactionRepositories
                .findByCustomerOrderByDateCreatedDesc(customer);
    }

    // SEARCH
    public List<Transaction> search(Customer customer, String keyword) {
        return transactionRepositories
                .findByCustomerAndDescriptionContainingIgnoreCaseOrderByDateCreatedDesc(
                        customer, keyword
                );
    }

    // LƯU TRANSACTION (DÙNG KHI MUA / NẠP)
    public Transaction save(Transaction transaction) {
        return transactionRepositories.save(transaction);
    }
}
