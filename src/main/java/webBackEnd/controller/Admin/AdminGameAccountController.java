package webBackEnd.controller.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import webBackEnd.entity.Game;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.Type;
import webBackEnd.service.*;
import webBackEnd.successfullyDat.PathCheck;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/adminHome")
public class AdminGameAccountController {

    @Autowired private CustomerService customerService;
    @Autowired private GameAccountService gameAccountService;
    @Autowired private GameService gameService;
    @Autowired private TypeService typeService;
    @Autowired private VoucherService voucherService;
    @Autowired private AdministratorService administratorService;
    @Autowired private PathCheck pathCheck;

    @GetMapping("/createGameAccount")
    public String createGameAccountForm(Model model) {

        model.addAttribute("games", gameService.findAllGame());
        model.addAttribute("types", typeService.getAllType());

        model.addAttribute("classifyList", List.of(
                "STUDENT", "BUDGET", "PREMIUM", "VIP"
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
                "BRONZE","SILVER","GOLD","PLATINUM","DIAMOND","HEROIC","GRANDMASTER"
        );

        model.addAttribute("rankAOV", rankAOV);
        model.addAttribute("rankFF", rankFF);

        model.addAttribute("gameAccount", new GameAccount());
        return "admin/GameAccountCreate";
    }

    @PostMapping("/saveNewGameAccount")
    @Transactional
    public String saveNewGameAccount(
            @RequestParam("gameAccount") String gameAccountName,
            @RequestParam("gamePassword") String gamePassword,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("classify") String classify,
            @RequestParam("status") String status,
            @RequestParam("rank") String rank,
            @RequestParam("duration") String duration,
            @RequestParam(value = "skin", defaultValue = "0") int skin,
            @RequestParam(value = "lovel", defaultValue = "0") int lovel,
            @RequestParam(value = "vip", defaultValue = "0") int vip,
            @RequestParam("gameId") UUID gameId,
            @RequestParam("imageFile") MultipartFile imageFile
    ) throws IOException {

        validateCreateGameAccountInput(
                gameAccountName, gamePassword, price, description,
                classify, status, rank, duration, skin, lovel, vip, gameId, imageFile
        );

        Game game = gameService.findById(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found with id: " + gameId);
        }

        Type type;
        if ("AOV".equalsIgnoreCase(game.getGameName())) {
            type = typeService.findByTypeName("MOBA");
        } else {
            type = typeService.findByTypeName("BATTLE ROYALE");
        }
        if (type != null) {
            game.setTypeId(type);
        }

        GameAccount ga = new GameAccount();
        ga.setGame(game);

        ga.setGameAccount(gameAccountName.trim());
        ga.setGamePassword(gamePassword.trim());
        ga.setPrice(price);
        ga.setDescription(description == null ? "" : description.trim());
        ga.setClassify(classify.trim());
        ga.setStatus(status.trim());
        ga.setRank(rank.trim());

        String d = duration == null ? "" : duration.trim().toUpperCase();
        if ("RENT".equals(d)) ga.setDuration("RENT");
        else if ("BUY".equals(d)) ga.setDuration(null);
        else throw new IllegalArgumentException("Invalid type. Allowed: BUY, RENT.");




        ga.setSkin(skin);
        ga.setLovel(lovel);
        ga.setVip(vip);
        ga.setCreatedDate(LocalDateTime.now());

        gameAccountService.save(ga);

        String folder = game.getGameName().equalsIgnoreCase("AOV") ? "aov/" : "ff/";

        String originalName = imageFile.getOriginalFilename();
        String ext = "jpg";
        if (originalName != null) {
            String lower = originalName.trim().toLowerCase();
            if (lower.endsWith(".png")) ext = "png";
            else if (lower.endsWith(".jpeg")) ext = "jpeg";
            else if (lower.endsWith(".jpg")) ext = "jpg";
        }

        String fileName = ga.getId().toString().toUpperCase() + "." + ext;

        String baseDir = pathCheck.getBaseDir();
        if (baseDir == null) {
            throw new IllegalStateException("Base directory is null (pathCheck.getBaseDir())");
        }
        if (!baseDir.endsWith("/") && !baseDir.endsWith("\\")) {
            baseDir = baseDir + "/";
        }

        Path dir = Paths.get(baseDir, "img", folder);
        Files.createDirectories(dir);

        Path outFile = dir.resolve(fileName);
        Files.write(outFile, imageFile.getBytes());

        ga.setImageMain(folder + fileName);
        gameAccountService.save(ga);

        return "redirect:/adminHome/gameList?nameGame=" + game.getGameName();
    }

    private void validateCreateGameAccountInput(
            String gameAccountName,
            String gamePassword,
            BigDecimal price,
            String description,
            String classify,
            String status,
            String rank,
            String duration,
            int skin,
            int lovel,
            int vip,
            UUID gameId,
            MultipartFile imageFile
    ) {

        if (gameAccountName == null || gameAccountName.trim().isEmpty()) {
            throw new IllegalArgumentException("Game username is required.");
        }
        if (gamePassword == null || gamePassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Game password is required.");
        }

        if (gameId == null) {
            throw new IllegalArgumentException("Game is required (gameId is null).");
        }

        if (price == null) {
            throw new IllegalArgumentException("Price is required.");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be > 0.");
        }

        Set<BigDecimal> allowed = new HashSet<>(List.of(
                new BigDecimal("50000"),
                new BigDecimal("100000"),
                new BigDecimal("150000"),
                new BigDecimal("200000")
        ));

        BigDecimal normalized = price.stripTrailingZeros();
        boolean okPrice = allowed.stream().anyMatch(a -> a.compareTo(normalized) == 0);
        if (!okPrice) {
            throw new IllegalArgumentException("Invalid price. Allowed: 50000, 100000, 150000, 200000.");
        }

        if (skin < 0) {
            throw new IllegalArgumentException("Skin must be >= 0.");
        }
        if (lovel < 0) {
            throw new IllegalArgumentException("Level must be >= 0.");
        }
        if (vip < 0) {
            throw new IllegalArgumentException("VIP must be >= 0.");
        }

        if (classify == null || classify.trim().isEmpty()) {
            throw new IllegalArgumentException("Classify is required.");
        }
        Set<String> classifyAllow = Set.of("STUDENT", "BUDGET", "PREMIUM", "VIP");
        if (!classifyAllow.contains(classify.trim().toUpperCase())) {
            throw new IllegalArgumentException("Invalid classify. Allowed: STUDENT, BUDGET, PREMIUM, VIP.");
        }

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required.");
        }
        Set<String> statusAllow = Set.of("ACTIVE", "INACTIVE", "HIDDEN");
        if (!statusAllow.contains(status.trim().toUpperCase())) {
            throw new IllegalArgumentException("Invalid status. Allowed: ACTIVE, INACTIVE, HIDDEN.");
        }

        if (duration == null || duration.trim().isEmpty()) {
            throw new IllegalArgumentException("Type is required (BUY/RENT).");
        }
        String d = duration.trim().toUpperCase();
        if (!("BUY".equals(d) || "RENT".equals(d))) {
            throw new IllegalArgumentException("Invalid type. Allowed: BUY, RENT.");
        }

        if (rank == null || rank.trim().isEmpty()) {
            throw new IllegalArgumentException("Rank is required.");
        }

        Game g = gameService.findById(gameId);
        if (g == null) {
            throw new IllegalArgumentException("Game not found for gameId: " + gameId);
        }

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
                "BRONZE","SILVER","GOLD","PLATINUM","DIAMOND","HEROIC","GRANDMASTER"
        );

        String gameName = (g.getGameName() == null) ? "" : g.getGameName().trim().toUpperCase();
        String rankInput = rank.trim().toUpperCase();

        if (gameName.equals("AOV")) {
            boolean ok = rankAOV.stream().anyMatch(x -> x.equalsIgnoreCase(rankInput));
            if (!ok) {
                throw new IllegalArgumentException("Invalid rank for AOV.");
            }
        } else if (gameName.equals("FREE FIRE") || gameName.equals("FREEFIRE")) {
            boolean ok = rankFF.stream().anyMatch(x -> x.equalsIgnoreCase(rankInput));
            if (!ok) {
                throw new IllegalArgumentException("Invalid rank for FREE FIRE.");
            }
        }

        if (imageFile == null) {
            throw new IllegalArgumentException("Image file is required (imageFile is null).");
        }
        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is required (file is empty).");
        }

        long maxBytes = 5L * 1024 * 1024;
        if (imageFile.getSize() > maxBytes) {
            throw new IllegalArgumentException("Image file is too large. Max 5MB.");
        }

        String originalName = imageFile.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid image file name.");
        }

        String ct = imageFile.getContentType();
        if (ct == null) {
            throw new IllegalArgumentException("Cannot detect image content type.");
        }
        String ctLower = ct.toLowerCase();
        if (!(ctLower.contains("jpeg") || ctLower.contains("jpg") || ctLower.contains("png"))) {
            throw new IllegalArgumentException("Only JPG/PNG images are allowed.");
        }

        if (description != null && description.length() > 2000) {
            throw new IllegalArgumentException("Description is too long (max 2000 chars).");
        }
    }
}
