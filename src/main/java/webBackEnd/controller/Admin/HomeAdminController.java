package webBackEnd.controller.Admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Game;
import webBackEnd.entity.GameAccount;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameAccountService;
import webBackEnd.service.GameService;

import java.util.UUID;

@Controller
@RequestMapping("/adminHome")
public class HomeAdminController {


    @Autowired
    private CustomerService customerService;

    @Autowired
    private GameAccountService gameAccountService;

    @Autowired
    private GameService gameService;


    @GetMapping("")
    public String homeAdmin(Model model) {
        return "admin/AdminIndex";
    }

    @GetMapping("/userList")
    public String userList(Model model) {

        model.addAttribute("listUser", customerService.findAllCustomers());
        return "admin/UserList";
    }

    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable("id") UUID id, Model model) {
        Customer customer = customerService.findCustomerById(id);
        model.addAttribute("user", customer);
        return "admin/UserDetail";
    }

    @GetMapping("/editusers/{id}")
    public String userUpdate(@PathVariable("id") UUID id, Model model) {
        Customer customer = customerService.findCustomerById(id);
        model.addAttribute("user", customer);
        return "admin/UserUpdate";
    }

    @GetMapping("/gameSelect")
    public String selectGameAccount(Model model) {

        model.addAttribute("AOV", "AOV");
        model.addAttribute("FF", "FF");
        return "admin/GameAccountSelect";
    }

    @GetMapping("/gameList")
    public String gameList(@RequestParam String nameGame, Model model)
        {
        Game game = gameService.findGameByGameName(nameGame);
//            for(GameAccount a : gameAccountService.findGameAccountByGame(game)){
//                System.out.println(a.getGameAccount());
//            }
        model.addAttribute("listGame", gameAccountService.findGameAccountByGame(game));
        return "admin/GameList";
    }

    @GetMapping("/gameDetail/{id}" )
    public String gameDetails(@PathVariable("id") UUID id, Model model){
        GameAccount gameAccount = gameAccountService.findGameAccountById(id);
        model.addAttribute("gameDetails", gameAccount);
        return "admin/GameDetail";
    }



}
