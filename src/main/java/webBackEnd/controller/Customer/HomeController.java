package webBackEnd.controller.Customer;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import webBackEnd.entity.*;
import webBackEnd.service.*;

import webBackEnd.successfullyDat.PathCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(value = "/home")
public class HomeController {


    @Autowired
    private GameAccountService gameAccountService;

    @Autowired
    private PathCheck pathCheck;
    @Autowired
    private GameService gameService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;

    @GetMapping
    public String home(Model model) {
//        for(GameAccount a : gameAccountService.get20Profuct()){
//            System.out.println(pathCheck.getPathWithOS(gameDetailService.getGameDetailByGameAccount(a).getMainImage()));
//        }
        model.addAttribute("list20Product", gameAccountService.get20Profuct());
        List<Game> game = gameService.findAllGame();
        model.addAttribute("game", game);
        return "customer/index";
    }


    @ModelAttribute("currentUser")
    public Customer currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerService.findCustomerByUsername(username);
    }


    @GetMapping("/profile/{id}")
    public String profile(Model model, @PathVariable("id") UUID id) {
        Customer customer = customerService.findCustomerById(id);
        List<Orders> order = ordersService.findAllByStatus("COMPLETED");
        List<GameAccount> gameAccounts = new ArrayList<>();
        for (Orders o : order) {
            for (OrderDetail e : orderDetailService.findAllByOrderId(o.getId())) {
                gameAccounts.add(e.getGameAccount());
            }
        }
        List<GameAccount> listGame =
                orderDetailService.getAllBoughtAccounts(id);
        model.addAttribute("listGame", gameAccounts);
        model.addAttribute("customer", customer);
        return "customer/ProfileUser";
    }


    @GetMapping("/wallet")
    public String wallet(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/home";
        }

        return "customer/wallet";
    }

    @GetMapping("/depositMoney")
    public String depositMoney(Model model) {
        return "customer/depositMoney";
    }

}
