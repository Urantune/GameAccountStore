package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.GameAccount;

import java.util.List;

@Repository
public interface GameAccountRepositories extends JpaRepository<GameAccount,Integer> {
    @Query("SELECT p.type, COUNT(p) FROM GameAccount p GROUP BY p.type")
    List<Object[]> countProductsByType();

}
