package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.GameAccount;
import webBackEnd.repository.OrderDetailRepositories;

import java.util.List;
import java.util.UUID;

@Service
public class OrderDetailService {

    @Autowired
    private OrderDetailRepositories orderDetailRepositories;
    public List<GameAccount> getAllBoughtAccounts(UUID customerId) {
        return orderDetailRepositories.findAllGameAccountBoughtByCustomer(customerId);
    }
}

