package webBackEnd.controller.Customer;

import jakarta.servlet.http.HttpSession;
import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.Orders;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameAccountService;
import webBackEnd.service.OrdersService;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(value = "/home")
public class BuyController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private GameAccountService gameAccountService;

    @GetMapping("/payment/{id}")
    public String checkout(@PathVariable("id") UUID id, Model model){
        GameAccount game = gameAccountService.findGameAccountById(id);
        model.addAttribute("games", game);
        return "customer/Payment";
    }

//    @PostMapping("/order/confirm/{id}")
//    public String requestOrders(@PathVariable("id") UUID id, @RequestParam("package") String packageName, HttpSession session, Model model){
//
//    }





}
