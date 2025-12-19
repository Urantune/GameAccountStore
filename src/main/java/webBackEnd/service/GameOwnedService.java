package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.GameOwned;
import webBackEnd.repository.GameOwnedRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameOwnedService {

    @Autowired
    private GameOwnedRepository repo;

    public boolean isOwned(Customer customer, GameAccount gameAccount) {
        if (customer == null || gameAccount == null) return false;
        return repo.existsByCustomerAndGameAccount(customer, gameAccount);
    }

    @Transactional
    public GameOwned createOwnedIfNotExists(Customer customer, GameAccount gameAccount) {
        if (customer == null || gameAccount == null) return null;

        if (repo.existsByCustomerAndGameAccount(customer, gameAccount)) {
            return null;
        }

        GameOwned owned = new GameOwned();
        owned.setCustomer(customer);
        owned.setGameAccount(gameAccount);
        owned.setDateOwned(LocalDateTime.now());

        return repo.save(owned);
    }

    public GameOwned save(GameOwned owned) {
        return repo.save(owned);
    }

    public List<GameOwned> findAllByCustomer(Customer customer) {
        return repo.findAllByCustomer(customer);
    }
}
