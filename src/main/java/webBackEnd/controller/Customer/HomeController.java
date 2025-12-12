package webBackEnd.controller.Customer;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Game;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameAccountService;

import webBackEnd.service.GameService;
import webBackEnd.service.WalletService;
import webBackEnd.successfullyDat.PathCheck;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(value = "/home")
public class HomeController {


    @Autowired
    private GameAccountService gameAccountService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PathCheck pathCheck;
    @Autowired
    private GameService gameService;

    @Autowired
    private WalletService walletService;

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

    @GetMapping("/news")
    public String news(Model model) {
        return "customer/news";
    }


}
