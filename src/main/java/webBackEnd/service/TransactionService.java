package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
}
