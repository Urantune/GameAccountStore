package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.RentAccountGame;
import webBackEnd.repository.RentAccountGameRepositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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


    public Optional<RentAccountGame> findByCustomerIdAndGameAccountId(
            UUID customerId,
            UUID gameAccountId
    ) {
        return rentAccountGameRepositories
                .findFirstByCustomer_CustomerIdAndGameAccount_GameAccountId(
                        customerId,
                        gameAccountId
                );
    }

    public void deleteByGameAccountId(UUID gameAccountId) {
        rentAccountGameRepositories.deleteByGameAccount_GameAccountId(gameAccountId);
    }

    public boolean existsByGameAccountId(UUID gameAccountId) {
        return rentAccountGameRepositories.existsByGameAccount_GameAccountId(gameAccountId);
    }

    public void delete(RentAccountGame rentAccountGame){
        rentAccountGameRepositories.delete(rentAccountGame);
    }

    public boolean isAccountRented(GameAccount account) {
        return rentAccountGameRepositories.existsByGameAccountAndDateEndAfterAndStatus(
                account,
                LocalDateTime.now(),
                "ACTIVE"   // đổi theo status của bạn
        );
    }
}
