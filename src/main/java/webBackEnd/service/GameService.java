package webBackEnd.service;

import jakarta.persistence.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Game;
import webBackEnd.entity.GameAccount;
import webBackEnd.repository.GameRepositories;

import java.util.List;
import java.util.UUID;

@Service
public class GameService {
    @Autowired
    private GameRepositories gameRepositories;
    public List<Game> findAllGame(){
        return gameRepositories.findAll();
    }

    public Game findById(UUID gameId){
        return gameRepositories.findByGameId(gameId).orElse(null);
    }

    public Game findGameByGameName(String gameName){
        return gameRepositories.findByGameName(gameName);
    }



}
