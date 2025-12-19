package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.GameOwned;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameOwnedRepository extends JpaRepository<GameOwned, UUID> {

    boolean existsByCustomerAndGameAccount(Customer customer, GameAccount gameAccount);

    List<GameOwned> findAllByCustomer(Customer customer);
}
