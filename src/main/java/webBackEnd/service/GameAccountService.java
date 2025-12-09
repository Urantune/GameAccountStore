package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.GameAccount;
import webBackEnd.repository.GameAccountRepositories;

import java.util.List;
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

}
