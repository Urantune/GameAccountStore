package webBackEnd.controller.Admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import webBackEnd.entity.*;
import webBackEnd.service.*;
import webBackEnd.successfullyDat.PathCheck;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
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


    @Autowired
    private VoucherService voucherService;

    @Autowired
    private AdministratorService administratorService;


    @Autowired
    private PathCheck pathCheck;


    @GetMapping("")
    public String homeAdmin(Model model) {
        return "admin/AdminIndex";
    }

    @GetMapping("/userList")
    public String userList(Model model) {
        System.out.println();
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
            @RequestParam(value = "duration", required = false) String duration,
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

        existing.setDuration((duration == null || duration.isBlank()) ? null : duration);

        existing.setImageMain(imageMain);
        existing.setRank(rank);
        existing.setSkin(skin);
        existing.setLovel(lovel);
        existing.setItems(items);
        existing.setUpdatedDate(LocalDateTime.now());

        Type type = typeService.findByTypeName(category);
        if (type == null) type = existing.getGame().getTypeId();

        Game game = existing.getGame();
        game.setTypeId(type);

        gameAccountService.save(existing);

        String gameName = existing.getGame().getGameName();
        return "redirect:/adminHome/gameList?nameGame=" + gameName;
    }



    @GetMapping("/listVoucher")
    public  String listVoucher(Model model) {

        model.addAttribute("listVoucher", voucherService.getAllVoucher());
        return  "admin/VoucherList";
    }

    @GetMapping("/voucherDetail/{id}")
    public String voucherDetail(@PathVariable("id") UUID id, Model model) {

        Voucher voucher = voucherService.getVoucherById(id);


        model.addAttribute("voucher", voucher);

        return "admin/VoucherDetail";
    }

    @GetMapping("/voucherUpdate/{id}")
    public String voucherUpdate(@PathVariable("id") UUID id, Model model) {

        Voucher voucher = voucherService.getVoucherById(id);
        if (voucher == null) {
            throw new RuntimeException("Voucher not found with id: " + id);
        }

        model.addAttribute("voucher", voucher);
        return "admin/VoucherUpdate";
    }

    @PostMapping("/saveVoucher")
    public String saveVoucherUpdate(
            @RequestParam("id") UUID id,
            @RequestParam("voucherName") String voucherName,
            @RequestParam("value") int value,
            @RequestParam("startDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date startDate,
            @RequestParam("endDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date endDate
    ) {

        Voucher existing = voucherService.getVoucherById(id);
        if (existing == null) {
            throw new RuntimeException("Voucher not found with id: " + id);
        }

        existing.setVoucherName(voucherName);
        existing.setValue(value);
        existing.setStartDate(startDate);
        existing.setEndDate(endDate);
        existing.setUpdateDate(LocalDateTime.now());


        voucherService.save(existing);

        return "redirect:/adminHome/listVoucher";
    }

    @GetMapping("/createVoucher")
    public String createVoucher(Model model) {

        Voucher voucher = new Voucher();
        model.addAttribute("voucher", voucher);
        return "admin/CreateVoucher";
    }

    @PostMapping("/saveNewVoucher")
    public String saveNewVoucher(
            @RequestParam String voucherName,
            @RequestParam int value,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        Voucher voucher = new Voucher();
        voucher.setVoucherName(voucherName);
        voucher.setValue(value);
        voucher.setStartDate(startDate);
        voucher.setEndDate(endDate);
        voucher.setUpdateDate(LocalDateTime.now());

        Staff s = administratorService.getStaffByID(UUID.fromString("88A7A905-CB27-431C-BFED-1D16BEA9B91B"));
        voucher.setStaff(s);

        voucherService.save(voucher);

        return "redirect:/adminHome/listVoucher";
    }







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
