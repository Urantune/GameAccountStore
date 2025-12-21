package webBackEnd.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import webBackEnd.entity.*;
import webBackEnd.successfullyDat.SendMailTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    @Autowired
    private GameAccountService gameAccountService;

    @Autowired
    private SendMailTest sendMailTest;

    @Scheduled(fixedDelay = 10_000)
    @Transactional
    public void checkBancle() {
        List<Customer> customers = customerService.findAllCustomers();

        for (Customer customer : customers) {
            List<Transaction> transactions = transactionService.findByCustomer(customer);

            BigDecimal total = BigDecimal.ZERO;
            BigDecimal addBlance = BigDecimal.ZERO;
                for(Transaction a : transactions){
                    if(a.getDescription().equalsIgnoreCase("TOPUP")){
                        addBlance.add(a.getAmount());
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
        List<RentAccountGame> rents = rentAccountGameService.findAll();

        List<RentAccountGame> toDelete = new ArrayList<>();

        for (RentAccountGame rent : rents) {

            if (rent.getDateEnd() == null) continue;

            if (!now.isBefore(rent.getDateEnd().plusDays(3))) {
                toDelete.add(rent);
                continue;
            }

            if (!now.isBefore(rent.getDateEnd().minusDays(3))) {

                if (!"EXPIRED".equalsIgnoreCase(rent.getStatus())) {

                    rent.setStatus("EXPIRED");
                    rentAccountGameService.save(rent);

                    GameAccount ga = rent.getGameAccount();
                    if (ga != null && !"EXPIRED".equalsIgnoreCase(ga.getStatus())) {
                        ga.setStatus("EXPIRED");
                        gameAccountService.save(ga);
                    }

                    Customer customer = rent.getCustomer();
                    if (customer != null && customer.getEmail() != null && ga != null) {

                        long daysLeft = Math.max(0, Duration.between(now, rent.getDateEnd()).toDays());

                        String body =
                                "<b>Kính chào quý khách,</b><br><br>" +
                                        "Tài khoản game bạn đang <b>THUÊ</b> đã được chuyển sang trạng thái <b>HẾT HẠN</b>.<br><br>" +
                                        "<b>TRÒ CHƠI:</b> " + (ga.getGame() != null ? ga.getGame().getGameName() : "") + "<br>" +
                                        "<b>TÀI KHOẢN:</b> " + ga.getGameAccount() + "<br>" +
                                        "<b>NGÀY HẾT HẠN:</b> " + rent.getDateEnd() + "<br>" +
                                        "<b>CÒN LẠI:</b> " + daysLeft + " ngày<br><br>" +
                                        "Vui lòng gia hạn nếu bạn muốn tiếp tục thuê.<br><br>" +
                                        "Xin cảm ơn.";

                        sendMailTest.testSend(customer.getEmail(), "TÀI KHOẢN THUÊ ĐÃ HẾT HẠN", body);
                    }
                }
            }
        }

        for (RentAccountGame r : toDelete) {
            rentAccountGameService.delete(r);
        }
    }


    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void revertChangePasswordAfter15m() {
        LocalDateTime now = LocalDateTime.now();

        List<Customer> customers = customerService.findAllCustomers();
        for (Customer c : customers) {

            if (c.getStatus() == null) continue;
            if (!c.getStatus().startsWith("CHANGE")) continue;
            if (c.getDateUpdated() == null) continue;

            if (c.getDateUpdated().plusMinutes(15).isBefore(now)) {
                c.setStatus("ACTIVE");
                c.setDateUpdated(now);
                customerService.save(c);
            }
        }
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void deleteWaitActiveAfter15m() {
        LocalDateTime now = LocalDateTime.now();

        List<Customer> customers = customerService.findAllCustomers();
        for (Customer c : customers) {

            if (c.getStatus() == null) continue;
            if (!"WAITACTIVE".equalsIgnoreCase(c.getStatus())) continue;
            if (c.getDateCreated() == null) continue;

            if (c.getDateCreated().plusMinutes(15).isBefore(now)) {
                customerService.delete(c.getUsername());
            }
        }
    }
}
