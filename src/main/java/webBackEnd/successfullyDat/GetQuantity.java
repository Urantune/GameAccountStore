package webBackEnd.successfullyDat;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import webBackEnd.entity.OrderDetail;
import webBackEnd.service.OrderDetailService;

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

}
