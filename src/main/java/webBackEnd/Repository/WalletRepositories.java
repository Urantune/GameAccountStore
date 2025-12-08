package webBackEnd.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.Entity.Wallet;

@Repository
public interface WalletRepositories extends JpaRepository<Wallet,Integer> {
}
