package webBackEnd.controller.Staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import webBackEnd.entity.OrderDetail;
import webBackEnd.entity.Orders;
import webBackEnd.service.AdministratorService;
import webBackEnd.service.OrderDetailService;
import webBackEnd.service.OrdersService;
import webBackEnd.successfullyDat.GetQuantity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("staffHome")
public class RentApproveController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private GetQuantity getQuantity;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private AdministratorService administratorService;


//    @GetMapping("/rentApproveList")
//    public String rentApproveList(Model model){
//        List<OrderDetail> list = new ArrayList<>();
//
//        model.addAttribute("rentOrderList",list);
//        model.addAttribute("getQuantity",getQuantity);
//        return "staff/RentApproveList";
//    }
//
//    @GetMapping("/rentApprove/{orderId}")
//    public String viewOrderDetail(@PathVariable UUID orderId, Model model) {
//        List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);
//
//        model.addAttribute("orderId", orderId);
//        model.addAttribute("orderDetails", orderDetails);
//
//        Orders order = ordersService.findById(orderId);
//        model.addAttribute("order", order);
//        return "staff/RentOrderDetail";
//    }


}
