package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.RentAccountGame;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RentAccountGameRepositories extends JpaRepository<RentAccountGame, UUID> {


    List<RentAccountGame> findAllByCustomer(Customer customer);

    Optional<RentAccountGame> findFirstByCustomer_CustomerIdAndGameAccount_GameAccountId(UUID customerId, UUID gameAccountId);

    void deleteByGameAccount_GameAccountId(UUID gameAccountId);
    boolean existsByGameAccount_GameAccountId(UUID gameAccountId);


}
