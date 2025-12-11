package webBackEnd.controller.Admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Game;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.Type;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameAccountService;
import webBackEnd.service.GameService;
import webBackEnd.service.TypeService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/adminHome")
public class HomeAdminController {


    @Autowired
    private CustomerService customerService;

    @Autowired
    private GameAccountService gameAccountService;

    @Autowired
    private GameService gameService;

    @Autowired
    private TypeService typeService;


    @GetMapping("")
    public String homeAdmin(Model model) {
        return "admin/AdminIndex";
    }

    @GetMapping("/userList")
    public String userList(Model model) {

        model.addAttribute("listUser", customerService.findAllCustomers());
        return "admin/UserList";
    }

    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable("id") UUID id, Model model) {
        Customer customer = customerService.findCustomerById(id);
        model.addAttribute("user", customer);
        return "admin/UserDetail";
    }


    @GetMapping("/editusers/{id}")
    public String userUpdate(@PathVariable("id") UUID id, Model model) {
        Customer customer = customerService.findCustomerById(id);
        model.addAttribute("user", customer);
        return "admin/UserUpdate";
    }

    @PostMapping("/saveUser")
    public String saveUserUpdate(
            @RequestParam("customerId") UUID customerId,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("role") String role,
            @RequestParam("status") String status) {

        Customer existing = customerService.findCustomerById(customerId);
        if (existing == null) {
            throw new RuntimeException("Customer not found with id: " + customerId);
        }

        existing.setUsername(username);
        existing.setEmail(email);
        existing.setRole(role);
        existing.setStatus(status);
        existing.setDateUpdated(LocalDateTime.now());

        customerService.save(existing);

        return "redirect:/adminHome/userList";
    }

    @GetMapping("/gameSelect")
    public String selectGameAccount(Model model) {

        model.addAttribute("AOV", "AOV");
        model.addAttribute("FF", "FF");
        return "admin/GameAccountSelect";
    }

    @GetMapping("/gameList")
    public String gameList(@RequestParam String nameGame, Model model)
        {
        Game game = gameService.findGameByGameName(nameGame);
//            for(GameAccount a : gameAccountService.findGameAccountByGame(game)){
//                System.out.println(a.getGameAccount());
//            }
        model.addAttribute("listGame", gameAccountService.findGameAccountByGame(game));
        return "admin/GameList";
    }

    @GetMapping("/gameDetail/{id}")
    public String gameDetails(@PathVariable("id") UUID id, Model model) {

        GameAccount gameAccount = gameAccountService.findGameAccountById(id);

        Game game = gameAccount.getGame();
        Type type = game.getTypeId();

        model.addAttribute("gameAccount", gameAccount);
        model.addAttribute("game", game);
        model.addAttribute("type", type);

        return "admin/GameDetail";
    }


    @GetMapping("/gameUpdate/{id}")
    public String gameUpdate(@PathVariable("id") UUID id, Model model) {
        GameAccount gameAccount = gameAccountService.findGameAccountById(id);

        Game game = gameAccount.getGame();
        Type type = game.getTypeId();

        model.addAttribute("gameAccount", gameAccount);
        model.addAttribute("game", game);
        model.addAttribute("type", type);

        return "admin/GameUpdate";
    }



    @PostMapping("/saveUpdate")
    public String saveUpdateGame(
            @RequestParam("id") UUID id,
            @RequestParam("gameAccount") String gameAccountName,
            @RequestParam("gamePassword") String gamePassword,
            @RequestParam("price") BigDecimal price,
            @RequestParam("description") String description,
            @RequestParam("classify") String classify,
            @RequestParam("status") String status,
            @RequestParam(value = "duration", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime duration,
            @RequestParam("imageMain") String imageMain,
            @RequestParam("rank") String rank,
            @RequestParam("skin") int skin,
            @RequestParam("lovel") int lovel,
            @RequestParam("items") int items,
            @RequestParam("category") String category
    ) {




        GameAccount existing = gameAccountService.findGameAccountById(id);
        if (existing == null) {
            throw new RuntimeException("GameAccount not found with id: " + id);
        }


        existing.setGameAccount(gameAccountName);
        existing.setGamePassword(gamePassword);
        existing.setPrice(price);
        existing.setDescription(description);
        existing.setClassify(classify);
        existing.setStatus(status);
        existing.setDuration(duration);
        existing.setImageMain(imageMain);
        existing.setRank(rank);
        existing.setSkin(skin);
        existing.setLovel(lovel);
        existing.setItems(items);
        existing.setUpdatedDate(LocalDateTime.now());


        Type type = typeService.findByTypeName(category);
        if (type == null) {
            type = existing.getGame().getTypeId();
        }

        Game game = existing.getGame();
        game.setTypeId(type);


        gameAccountService.save(existing);


        String gameName = existing.getGame().getGameName();
        return "redirect:/adminHome/gameList?nameGame=" + gameName;
    }



}
