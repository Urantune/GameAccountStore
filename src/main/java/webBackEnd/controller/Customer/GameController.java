package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import webBackEnd.entity.Game;
import webBackEnd.entity.GameAccount;
import webBackEnd.service.GameAccountService;
import webBackEnd.service.GameService;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(value = "/home")
public class GameController {
    @Autowired
    private GameAccountService gameAccountService;
    @Autowired
    private GameService gameService;
    @GetMapping("/game/{gameId}")
    public String gameAOV(Model model, @PathVariable("gameId") UUID gameId){
        List<GameAccount> game = gameAccountService.findAllByGameId(gameId);
        model.addAttribute("gameId", game);
        return "customer/GameAccount";
    }

    @GetMapping("/gameDetail/{id}")
    public String gameDetail(@PathVariable UUID id, Model model) {
        GameAccount p = gameAccountService.findGameAccountById(id);
        model.addAttribute("p", p);
        return "customer/GameDetail";
    }

}
