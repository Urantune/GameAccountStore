package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Game;
import webBackEnd.entity.GameAccount;
import webBackEnd.repository.GameAccountRepositories;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class GameAccountService {

    @Autowired
    private GameAccountRepositories gameAccountRepositories;


    public List<GameAccount> get20Profuct(){
        return gameAccountRepositories.findRandom20();
    }

    public GameAccount findGameAccountById(UUID id){
        return gameAccountRepositories.getById(id);
    }

    public List<GameAccount> findAllByGameId(UUID gameId){
        return gameAccountRepositories.findAllByGame_GameId(gameId);
    }

    public List<GameAccount> findGameAccountByGame(Game game){
        return gameAccountRepositories.findAllByGame(game);
    }

    public void save(GameAccount gameAccount){
        gameAccountRepositories.save(gameAccount);
    }
    public GameAccount getGameById(UUID gameId) {
        return gameAccountRepositories.findById(gameId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy game"));
    }


    public Set<UUID> getSoldOrRentedAccountIds() {
        Set<UUID> ids = new HashSet<>();

        gameAccountRepositories.findAllSoldAccounts()
                .forEach(acc -> ids.add(acc.getId()));

        gameAccountRepositories.findAllRentedAccounts()
                .forEach(acc -> ids.add(acc.getId()));

        return ids;
    }

    public List<GameAccount> getPrice50() {
        return gameAccountRepositories.findByPrice(BigDecimal.valueOf(50));
    }

    public List<GameAccount> getPrice100() {
        return gameAccountRepositories.findByPrice(BigDecimal.valueOf(100));
    }

    public List<GameAccount> getPrice150() {
        return gameAccountRepositories.findByPrice(BigDecimal.valueOf(150));
    }

    public List<GameAccount> getPrice200() {
        return gameAccountRepositories.findByPrice(BigDecimal.valueOf(200));
    }

    public List<GameAccount> findExpired() {
        return gameAccountRepositories.findByStatus("EXPIRED");
    }



}
