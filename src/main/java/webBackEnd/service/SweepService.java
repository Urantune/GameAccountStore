package webBackEnd.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import webBackEnd.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SweepService {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private RentAccountGameService rentAccountGameService;

    @Autowired
    private TransactionService transactionService;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void checkBancle() {
        List<Customer> customers = customerService.findAllCustomers();

        for (Customer customer : customers) {
            List<Transaction> transactions = transactionService.findByCustomer(customer);

            BigDecimal total = BigDecimal.ZERO;
            for (Transaction t : transactions) {
                if (t.getAmount() != null) {
                    total = total.add(t.getAmount());
                }
            }

            customer.setBalance(total);
            customerService.save(customer);
        }
    }


    @Scheduled(fixedDelay = 120_000)
    @Transactional
    public void rent() {

        LocalDateTime now = LocalDateTime.now();

        List<RentAccountGame> rentAccountGames = rentAccountGameService.findAll();

        for(RentAccountGame a : rentAccountGames){
            if(now.minusDays(3).isBefore(a.getDateEnd())){
            a.setStatus("EXPIRING");
            }

            if(now.plusDays(3).isBefore(a.getDateEnd())){
                rentAccountGameService.delete(a);
            }
        }



    }


}
