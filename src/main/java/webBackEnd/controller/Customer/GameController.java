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

        List<GameAccount> gameAccounts = gameAccountService.findAllByGameId(gameId);
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
        // ===== ACC ĐÃ BÁN / ĐÃ THUÊ =====
        Set<UUID> soldOrRentedIds = gameAccountService.getSoldOrRentedAccountIds();
        gameAccounts.sort((a, b) -> {
            boolean aSold = soldOrRentedIds.contains(a.getId());
            boolean bSold = soldOrRentedIds.contains(b.getId());
            return Boolean.compare(aSold, bSold);
        });
        // ===== TRUYỀN SANG VIEW =====
        model.addAttribute("accounts", gameAccounts);
        model.addAttribute("soldOrRentedIds", soldOrRentedIds);
        model.addAttribute("pageTitle", title);

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

}
