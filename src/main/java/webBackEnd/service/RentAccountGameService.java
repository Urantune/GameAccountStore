package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.RentAccountGame;
import webBackEnd.repository.RentAccountGameRepositories;

@Service
public class RentAccountGameService {

    @Autowired
    private RentAccountGameRepositories rentAccountGameRepositories;


    public RentAccountGame save(RentAccountGame rentAccountGame){
        return rentAccountGameRepositories.save(rentAccountGame);
    }


}
