package webBackEnd.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.Entity.GameAccount;
@Repository
public interface GameAccountRepositories extends JpaRepository<GameAccount,Integer> {
}
