package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.RentAccountGame;
import webBackEnd.repository.RentAccountGameRepositories;

import java.util.List;
import java.util.UUID;

@Service
public class RentAccountGameService {

    @Autowired
    private RentAccountGameRepositories rentAccountGameRepositories;


    public RentAccountGame save(RentAccountGame rentAccountGame){
        return rentAccountGameRepositories.save(rentAccountGame);
    }


    public RentAccountGame findById(UUID id){
        return rentAccountGameRepositories.findById(id).orElse(null);
    }



    public List<RentAccountGame> findAll(){
        return rentAccountGameRepositories.findAll();
    }


    public void delete(RentAccountGame rentAccountGame){
        rentAccountGameRepositories.delete(rentAccountGame);
    }



}
