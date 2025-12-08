package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Wallet;

@Repository
public interface WalletRepositories extends JpaRepository<Wallet,Integer> {
}
