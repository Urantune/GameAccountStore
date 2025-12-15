package webBackEnd.successfullyDat;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import webBackEnd.entity.OrderDetail;
import webBackEnd.service.OrderDetailService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class GetQuantity {

    @Autowired
    private OrderDetailService  orderDetailService;

    public int findAllByOrderId(UUID orderId) {
        List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);
        return orderDetails.size();
    }

    public static Integer subtract( LocalDateTime end) {

        LocalDateTime start = LocalDateTime.now();
        int min = end.getMinute() - start.getMinute();
        int hour = end.getHour() - start.getHour();
        int day = end.getDayOfMonth() - start.getDayOfMonth();
        int month = end.getMonthValue() - start.getMonthValue();
        int year = end.getYear() - start.getYear();

        day += year *365;
        day += month *30;
        day += hour / 24;
        day += min / 1440;

        if(day <0){
            return 0;
        }
       return day;
    }

}
