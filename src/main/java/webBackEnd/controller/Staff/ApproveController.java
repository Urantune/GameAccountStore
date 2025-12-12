package webBackEnd.controller.Staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import webBackEnd.entity.Orders;
import webBackEnd.service.OrdersService;
import webBackEnd.successfullyDat.GetQuantity;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/staffHome")
public class ApproveController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private GetQuantity getQuantity;


    @GetMapping("/approveList")
    public String approveList(Model model){

        List<Orders> list = ordersService.findAllByStatus("WAIT");
        list.sort(Comparator.comparing(Orders::getCreatedDate));



        model.addAttribute("orderList",list);
        model.addAttribute("getQuantity",getQuantity);
        return "staff/ApproveList";
    }


}
