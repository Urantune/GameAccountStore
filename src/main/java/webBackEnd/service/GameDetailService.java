package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.GameDetail;
import webBackEnd.repository.GameDetailRepositories;

import java.util.UUID;

@Service
public class GameDetailService {

    @Autowired
    private GameDetailRepositories gameDetailRepositories;


    public GameDetail getGameDetailByGameAccount(GameAccount gameAccount) {
        return gameDetailRepositories.findByGameAccount(gameAccount);
    }
}
