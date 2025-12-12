package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameAccountService;

import java.util.UUID;

@Controller
@RequestMapping(value = "/home")
public class BuyController {
    @Autowired
    private CustomerService customerService;

    @Autowired
    private GameAccountService gameAccountService;

    @GetMapping("/payment/{id}")
    public String checkout(@PathVariable("id") UUID id, Model model){
        GameAccount game = gameAccountService.findGameAccountById(id);
        model.addAttribute("game", game);
        return "customer/Payment";
    }

//    @GetMapping("/buyGame/{id}")
//    public String buyGame(Model model,@PathVariable UUID id) {
//        GameAccount gameAccount = gameAccountService.findGameAccountById(id);
//        Customer customer = customerService.findCustomerById(id);
//        if(customer.getBalance()<){
//
//        }
//
//    }


}
