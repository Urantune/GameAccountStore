package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.GameAccount;
import webBackEnd.repository.GameAccountRepositories;

import java.util.List;

@Service
public class GameAccountService {

    @Autowired
    private GameAccountRepositories gameAccountRepositories;


    public List<GameAccount> get20Profuct(){
        return gameAccountRepositories.findRandom20();
    }
}
