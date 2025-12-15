package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.RentAccountGame;

import java.util.List;
import java.util.UUID;

@Repository
public interface RentAccountGameRepositories extends JpaRepository<RentAccountGame, UUID> {
    @Query("""
    SELECT DISTINCT r.gameAccount
    FROM RentAccountGame r
""")
    List<GameAccount> findAllRentedOrSoldAccounts();

}
