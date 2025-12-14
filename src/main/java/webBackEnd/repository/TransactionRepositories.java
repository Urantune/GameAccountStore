package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Transaction;


import java.util.UUID;

@Repository
public interface TransactionRepositories extends JpaRepository<Transaction,UUID> {



}
