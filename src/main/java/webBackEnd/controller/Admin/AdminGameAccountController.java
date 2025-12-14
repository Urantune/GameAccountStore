package webBackEnd.controller.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import webBackEnd.entity.Game;
import webBackEnd.entity.GameAccount;
import webBackEnd.service.*;
import webBackEnd.successfullyDat.PathCheck;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/adminHome")
public class AdminGameAccountController {


    @Autowired
    private CustomerService customerService;

    @Autowired
    private GameAccountService gameAccountService;

    @Autowired
    private GameService gameService;

    @Autowired
    private TypeService typeService;


    @Autowired
    private VoucherService voucherService;

    @Autowired
    private AdministratorService administratorService;


    @Autowired
    private PathCheck pathCheck;



    @GetMapping("/createGameAccount")
    public String createGameAccountForm(Model model) {


        model.addAttribute("games", gameService.findAllGame());


        model.addAttribute("types", typeService.getAllType());


        model.addAttribute("classifyList", List.of(
                "STUDENT",
                "BUDGET",
                "PREMIUM",
                "VIP"
        ));


        List<String> rankAOV = List.of(
                "UNRANKED",
                "BRONZE III","BRONZE II","BRONZE I",
                "SILVER III","SILVER II","SILVER I",
                "GOLD III","GOLD II","GOLD I",
                "PLATINUM III","PLATINUM II","PLATINUM I",
                "DIAMOND III","DIAMOND II","DIAMOND I",
                "MASTER","CONQUEROR"
        );


        List<String> rankFF = List.of(
                "BRONZE",
                "SILVER",
                "GOLD",
                "PLATINUM",
                "DIAMOND",
                "HEROIC",
                "GRANDMASTER"
        );

        model.addAttribute("rankAOV", rankAOV);
        model.addAttribute("rankFF", rankFF);

        model.addAttribute("gameAccount", new GameAccount());

        return "admin/GameAccountCreate";
    }


    @PostMapping("/saveNewGameAccount")
    public String saveNewGameAccount(
            @RequestParam("gameAccount") String gameAccountName,
            @RequestParam("gamePassword") String gamePassword,
            @RequestParam("price") BigDecimal price,
            @RequestParam("description") String description,
            @RequestParam("classify") String classify,
            @RequestParam("status") String status,
            @RequestParam("rank") String rank,
            @RequestParam("skin") int skin,
            @RequestParam("lovel") int lovel,
            @RequestParam("vip") int vip,
            @RequestParam("gameId") UUID gameId,
            @RequestParam("imageFile") MultipartFile imageFile
    ) throws IOException {

        Game game = gameService.findById(gameId);

        GameAccount ga = new GameAccount();
        ga.setGame(game);

        ga.setGameAccount(gameAccountName);
        ga.setGamePassword(gamePassword);
        ga.setPrice(price);
        ga.setDescription(description);
        ga.setClassify(classify);
        ga.setStatus(status);
        ga.setRank(rank);
        ga.setSkin(skin);
        ga.setLovel(lovel);
        ga.setItems(vip);
        ga.setCreatedDate(LocalDateTime.now());


        gameAccountService.save(ga);


        String folder = game.getGameName().equalsIgnoreCase("AOV") ? "aov/" : "ff/";
        String fileName = ga.getId().toString().toUpperCase() + ".jpg";
        String savePath = pathCheck.getBaseDir() + "img/" + folder;

        Files.createDirectories(Paths.get(savePath));
        Files.write(Paths.get(savePath + fileName), imageFile.getBytes());

        ga.setImageMain( folder + fileName);



        return "redirect:/adminHome/gameList?nameGame=" + game.getGameName();
    }
}
