package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.GameAccount;
@Repository
public interface GameAccountRepositories extends JpaRepository<GameAccount,Integer> {
}
