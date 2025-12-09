package webBackEnd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import webBackEnd.entity.GameAccount;
import webBackEnd.service.GameAccountService;
import webBackEnd.service.GameDetailService;

import java.util.UUID;

@Controller
@RequestMapping("/home")
public class GameDetailController {

    @Autowired
    private GameAccountService gameAccountService;
    @Autowired
    private GameDetailService gameDetailService;


    @GetMapping("/detail/{id}")
    public String showGameDetail(@PathVariable("id") UUID id, Model model) {
        GameAccount game = gameAccountService.findGameAccountById(id);

        if (game == null) {

            return "redirect:/game/list";
        }


        model.addAttribute("game", game);


        return "GameAccountDetail";
    }
}
