package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import webBackEnd.entity.Game;
import webBackEnd.service.GameAccountService;

import webBackEnd.service.GameService;
import webBackEnd.successfullyDat.PathCheck;

import java.util.List;

@Controller
@RequestMapping(value = "/home")
public class HomeController {


    @Autowired
    private GameAccountService gameAccountService;

    @Autowired
    private PathCheck pathCheck;
    @Autowired
    private GameService gameService;

    @GetMapping
    public String home(Model model) {
//        for(GameAccount a : gameAccountService.get20Profuct()){
//            System.out.println(pathCheck.getPathWithOS(gameDetailService.getGameDetailByGameAccount(a).getMainImage()));
//        }
        model.addAttribute("list20Product", gameAccountService.get20Profuct());
        List<Game> game = gameService.findAllGame();
        model.addAttribute("game", game);
        return "index";
    }

}
