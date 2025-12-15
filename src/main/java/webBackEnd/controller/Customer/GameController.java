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
import webBackEnd.repository.GameAccountRepositories;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameAccountService;
import webBackEnd.service.GameService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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
    @GetMapping("/game/{gameId}")
    public String gameAccount(Model model, @PathVariable UUID gameId) {

        List<GameAccount> gameAccounts = gameAccountService.findAllByGameId(gameId);
        Game game = gameService.findById(gameId);
        UUID AOV_ID = UUID.fromString("E8301A2F-AEB4-42FB-9C3C-41B16D3DEA8D");
        UUID FF_ID  = UUID.fromString("B1CF2298-5C85-4FBA-920B-63C028131163");
        String title = "";
        if (game.getGameId().equals(AOV_ID)) {
            title = "Tài khoản Game AOV";
        } else if (game.getGameId().equals(FF_ID)) {
            title = "Tài khoản Game Free Fire";
        }
        model.addAttribute("pageTitle", title);
        model.addAttribute("gameId", gameAccounts);

        return "customer/GameAccount";
    }


    @GetMapping("/gameDetail/{id}")
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

    @GetMapping("/game")
    public String filterGameAccount(
            @RequestParam(required = false) String duration,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) Integer vip,
            @RequestParam(required = false) String rank,
            @RequestParam(required = false) String skins,
            @RequestParam(required = false) String level,
            Model model
    ) {

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        if (price != null && !price.isBlank()) {
            if (price.endsWith("+")) {
                minPrice = new BigDecimal(price.replace("+", ""));
            } else {
                String[] p = price.split("-");
                minPrice = new BigDecimal(p[0]);
                maxPrice = new BigDecimal(p[1]);
            }
        }

        Integer minSkin = null, maxSkin = null;
        if (skins != null && !skins.isBlank()) {
            if (skins.endsWith("+")) {
                minSkin = Integer.parseInt(skins.replace("+", ""));
            } else {
                String[] s = skins.split("-");
                minSkin = Integer.parseInt(s[0]);
                maxSkin = Integer.parseInt(s[1]);
            }
        }

        Integer minLevel = null, maxLevel = null;
        if (level != null && !level.isBlank()) {
            if (level.endsWith("+")) {
                minLevel = Integer.parseInt(level.replace("+", ""));
            } else {
                String[] l = level.split("-");
                minLevel = Integer.parseInt(l[0]);
                maxLevel = Integer.parseInt(l[1]);
            }
        }

        List<GameAccount> list = gameAccountRepositories.filterGameAccount(
                duration, minPrice, maxPrice, vip, rank, minSkin, maxSkin, minLevel, maxLevel
        );

        model.addAttribute("game", list);
        return "customer/GameAccount";
    }


}
