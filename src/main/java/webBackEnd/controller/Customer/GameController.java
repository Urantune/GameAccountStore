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

        // ===== TITLE =====
        UUID AOV_ID = UUID.fromString("E8301A2F-AEB4-42FB-9C3C-41B16D3DEA8D");
        UUID FF_ID  = UUID.fromString("B1CF2298-5C85-4FBA-920B-63C028131163");

        String title = "Danh sách tài khoản";
        if (game.getGameId().equals(AOV_ID)) {
            title = "Tài khoản Game AOV";
        } else if (game.getGameId().equals(FF_ID)) {
            title = "Tài khoản Game Free Fire";
        }

        // ===== 4 MỨC GIÁ CỐ ĐỊNH =====
        List<BigDecimal> prices = List.of(
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(150000),
                BigDecimal.valueOf(200000)
        );
        model.addAttribute("pageTitle", title);
        model.addAttribute("gameId", gameId);
        model.addAttribute("gameName", game.getGameName());
        model.addAttribute("prices", prices);

        return "customer/GameAccount";
    }



    @GetMapping("/GameDetail/{id}")
    public String gameDetail(@PathVariable UUID id, Model model) {
        GameAccount p = gameAccountService.findGameAccountById(id);
        model.addAttribute("p", p);
        return "customer/GameDetail";
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
        model.addAttribute("gameId", gameId); // để dùng sau

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

        // Random Level & VIP
        int randomLevel = (int) (Math.random() * 50) + 1;
        int randomVip = (int) (Math.random() * 10) + 1;

        model.addAttribute("price", price);
        model.addAttribute("gameName", game);
        model.addAttribute("skinMin", skinMin);
        model.addAttribute("skinMax", skinMax);
        model.addAttribute("level", randomLevel);
        model.addAttribute("vip", randomVip);
        model.addAttribute("gameName", game);
        return "customer/GameDetail";
    }



}
