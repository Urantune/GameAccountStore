package webBackEnd.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Customer;
import webBackEnd.entity.OrderDetail;
import webBackEnd.entity.Orders;
import webBackEnd.entity.RentAccountGame;

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

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void order() {
        LocalDateTime now = LocalDateTime.now();

        List<Orders> orders = ordersService.findAllByStatus("WAIT");

        for(Orders order:orders){
            if(order.getCreatedDate().plusYears(300).isBefore(now)){
                List<OrderDetail> list = orderDetailService.findAllByOrderId(order.getId());
                for(OrderDetail orderDetail:list){
                    orderDetailService.delete(orderDetail);
                }
                ordersService.delete(order);
            }

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
