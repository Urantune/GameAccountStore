package webBackEnd.controller.Customer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Game;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.RentAccountGame;
import webBackEnd.repository.GameAccountRepositories;
import webBackEnd.repository.RentAccountGameRepositories;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameAccountService;
import webBackEnd.service.GameService;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/home")
public class GameController {
    @Autowired
    private GameAccountService gameAccountService;
    @Autowired
    private GameService gameService;
    @Autowired
    private CustomerService  customerService;
    @Autowired
    private GameAccountRepositories gameAccountRepositories;
    @Autowired
    private RentAccountGameRepositories  rentAccountGameRepositories;
    @GetMapping("/game/{gameId}")
    public String gameAccount(Model model, @PathVariable UUID gameId) {

        Game game = gameService.findById(gameId);

        List<BigDecimal> prices = List.of(
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(150000),
                BigDecimal.valueOf(200000)
        );

        model.addAttribute("game", game);
        model.addAttribute("gameId", gameId);
        model.addAttribute("gameName", game.getGameName());
        model.addAttribute("prices", prices);

        return "customer/GameAccount";
    }

    @ModelAttribute("currentUser")
    public Customer currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerService.findCustomerByUsername(username);
    }

    @GetMapping("/home/products")
    public String products(
            @RequestParam(required = false) UUID gameId,
            Model model) {

        List<BigDecimal> prices = List.of(
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(150000),
                BigDecimal.valueOf(200000)
        );

        model.addAttribute("prices", prices);
        model.addAttribute("gameId", gameId);

        return "customer/GameAccount";
    }

    @GetMapping("/GameDetail")
    public String gameDetail(@RequestParam int price,
                             @RequestParam String game,
                             Model model) {

        int skinMin = 0;
        int skinMax = 0;

        switch (price) {
            case 50000:
                skinMin = 10;
                skinMax = 30;
                break;
            case 100000:
                skinMin = 30;
                skinMax = 60;
                break;
            case 150000:
                skinMin = 60;
                skinMax = 90;
                break;
            case 200000:
                skinMin = 90;
                skinMax = 150;
                break;
        }

        int randomLevel = (int) (Math.random() * 50) + 1;
        int randomVip = (int) (Math.random() * 10) + 1;

        model.addAttribute("price", price);
        model.addAttribute("gameName", game);
        model.addAttribute("gameId", gameService.findGameByGameName(game).getGameId());
        model.addAttribute("skinMin", skinMin);
        model.addAttribute("skinMax", skinMax);
        model.addAttribute("level", randomLevel);
        model.addAttribute("vip", randomVip);
        model.addAttribute("gameName", game);

        return "customer/GameDetail";
    }



    @GetMapping("/GameDetails")
    public String gameByPrice(
            @RequestParam BigDecimal price,
            @RequestParam String game,
            Model model
    ) {
        List<GameAccount> accounts =
                gameAccountService.getByPriceAndGame(game, price);

        model.addAttribute("accounts", accounts);
        model.addAttribute("price", price);
        model.addAttribute("gameName", game);

        return "customer/AccountByPrice";
    }


    @GetMapping("/accDetail/{id}")
    public String gameDetail(@PathVariable UUID id, Model model, @RequestParam String game) {
        GameAccount p = gameAccountService.findGameAccountById(id);
        model.addAttribute("p", p);
        model.addAttribute("gameName", game);
        return "customer/GameDetail";
    }


}
