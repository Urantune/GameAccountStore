package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Game;
import webBackEnd.entity.GameAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface GameAccountRepositories extends JpaRepository<GameAccount, UUID> {



    @Query(value = "SELECT TOP 20 * FROM GameAccount ORDER BY NEWID()", nativeQuery = true)
    List<GameAccount> findRandom20();

    GameAccount getById(UUID id);

    List<GameAccount> findAllByGame_GameId(UUID gameId);

    List<GameAccount> findAllByGame(Game game);



    @Query("""
SELECT g FROM GameAccount g
WHERE (:duration IS NULL OR g.duration = :duration)
AND (:vip IS NULL OR g.vip = :vip)
AND (:rank IS NULL OR g.rank = :rank)
AND (:minPrice IS NULL OR g.price >= :minPrice)
AND (:maxPrice IS NULL OR g.price <= :maxPrice)
AND (:minSkin IS NULL OR g.skin >= :minSkin)
AND (:maxSkin IS NULL OR g.skin <= :maxSkin)
AND (:minLevel IS NULL OR g.lovel >= :minLevel)
AND (:maxLevel IS NULL OR g.lovel <= :maxLevel)
""")
    List<GameAccount> filterGameAccount(
            @Param("duration") String duration,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("vip") Integer vip,
            @Param("rank") String rank,
            @Param("minSkin") Integer minSkin,
            @Param("maxSkin") Integer maxSkin,
            @Param("minLevel") Integer minLevel,
            @Param("maxLevel") Integer maxLevel
    );

}
