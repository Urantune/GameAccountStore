package webBackEnd.service;

import jakarta.persistence.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Game;
import webBackEnd.repository.GameRepositories;

import java.util.List;

@Service
public class GameService {
    @Autowired
    private GameRepositories gameRepositories;
    public List<Game> findAllGame(){
        return gameRepositories.findAll();
    }
}
