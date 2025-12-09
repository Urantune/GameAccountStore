package webBackEnd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import webBackEnd.entity.GameAccount;
import webBackEnd.repository.GameAccountRepositories;
import webBackEnd.service.GameAccountService;
import webBackEnd.service.GameDetailService;
import webBackEnd.successfullyDat.PathCheck;

@Controller
@RequestMapping(value = "/home")
public class HomeController {


    @Autowired
    private GameAccountService gameAccountService;

    @Autowired
    private GameDetailService gameDetailService;
    @Autowired
    private PathCheck pathCheck;


    @GetMapping
    public String home(Model model) {



//        for(GameAccount a : gameAccountService.get20Profuct()){
//            System.out.println(pathCheck.getPathWithOS(gameDetailService.getGameDetailByGameAccount(a).getMainImage()));
//        }


        model.addAttribute("list20Product", gameAccountService.get20Profuct());

        return "index";
    }
}
