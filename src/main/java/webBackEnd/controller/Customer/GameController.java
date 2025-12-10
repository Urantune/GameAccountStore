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

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(value = "/home")
public class GameController {
    @Autowired
    private GameAccountService gameAccountService;
    @GetMapping("/game/{gameId}")
    public String gameAOV(Model model, @PathVariable("gameId") UUID gameId){
        List<GameAccount> game = gameAccountService.findAllByGameId(gameId);
        model.addAttribute("gameId", game);
        return "customer/GameAccount";
    }

//    @GetMapping("/gameDetail/{id}")
//    public String gameDetail(Model model, @RequestParam("gameDetail") UUID gameId){
//        List<GameAccount> gameDetail = gameAccountService.findAllByGameId(gameId);
//        Game game = new Game();
//        model.addAttribute("gameDetail", gameDetail);
//        model.addAttribute("gameName", game);
//        return "customer/GameDetail";
//    }
}
