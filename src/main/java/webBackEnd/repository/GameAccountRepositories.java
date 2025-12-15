package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Game;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.Orders;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameAccountRepositories extends JpaRepository<GameAccount, UUID> {



    @Query(value = "SELECT TOP 20 * FROM GameAccount ORDER BY NEWID()", nativeQuery = true)
    List<GameAccount> findRandom20();

    GameAccount getById(UUID id);

    List<GameAccount> findAllByGame_GameId(UUID gameId);

    List<GameAccount> findAllByGame(Game game);





}
