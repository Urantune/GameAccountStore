package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.Customer;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.GameOwned;
import webBackEnd.entity.RentAccountGame;
import webBackEnd.service.CustomerService;
import webBackEnd.service.GameOwnedService;
import webBackEnd.service.RentAccountGameService;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/home")
public class ListAccController {

    @Autowired private CustomerService customerService;
    @Autowired private GameOwnedService gameOwnedService;
    @Autowired private RentAccountGameService rentAccountGameService;

    @GetMapping("/listAcc")
    public String listAcc(@RequestParam(value = "game", required = false) String game, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null ? auth.getName() : null);

        if (username == null || "anonymousUser".equals(username)) {
            model.addAttribute("listAcc", List.of());
            model.addAttribute("errorMessage", "Vui lòng đăng nhập.");
            model.addAttribute("gameName", game);
            return "customer/ListAcc";
        }
        Customer customer = null;
        try { customer = customerService.findCustomerByUsername(username); } catch (Exception ignored) {}

        if (customer == null) {
            model.addAttribute("listAcc", List.of());
            model.addAttribute("errorMessage", "Không tìm thấy thông tin khách hàng.");
            model.addAttribute("gameName", game);
            return "customer/ListAcc";
        }
        String gameFilter = (game == null ? null : game.trim());
        List<Map<String, Object>> tmp = new ArrayList<>();
        List<GameOwned> ownedList = null;
        try { ownedList = gameOwnedService.findAllByCustomer(customer); } catch (Exception ignored) {}
        if (ownedList != null) {
            for (GameOwned go : ownedList) {
                if (go == null || go.getGameAccount() == null) continue;
                GameAccount ga = go.getGameAccount();

                if (gameFilter != null && !gameFilter.isBlank()) {
                    if (ga.getGame() == null || ga.getGame().getGameName() == null) continue;
                    if (!ga.getGame().getGameName().equalsIgnoreCase(gameFilter)) continue;
                }

                Map<String, Object> m = new HashMap<>();
                m.put("type", "O");
                m.put("date", go.getDateOwned());
                m.put("ga", ga);
                tmp.add(m);
            }
        }
        List<RentAccountGame> rentList = null;
        try { rentList = rentAccountGameService.findAllByCustomer(customer); } catch (Exception ignored) {}
        if (rentList != null) {
            for (RentAccountGame rg : rentList) {
                if (rg == null || rg.getGameAccount() == null) continue;
                GameAccount ga = rg.getGameAccount();

                if (gameFilter != null && !gameFilter.isBlank()) {
                    if (ga.getGame() == null || ga.getGame().getGameName() == null) continue;
                    if (!ga.getGame().getGameName().equalsIgnoreCase(gameFilter)) continue;
                }

                Map<String, Object> m = new HashMap<>();
                m.put("type", "R");
                m.put("date", rg.getDateStart());
                m.put("ga", ga);
                tmp.add(m);
            }
        }
        tmp.sort((a, b) -> {
            LocalDateTime da = (LocalDateTime) a.get("date");
            LocalDateTime db = (LocalDateTime) b.get("date");
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return db.compareTo(da);
        });
        List<Map<String, GameAccount>> rows = new ArrayList<>();
        for (Map<String, Object> t : tmp) {
            String type = (String) t.get("type");
            GameAccount ga = (GameAccount) t.get("ga");
            if (type == null || ga == null) continue;
            Map<String, GameAccount> item = new HashMap<>();
            item.put(type, ga);
            rows.add(item);
        }
        model.addAttribute("currentUser", customer);
        model.addAttribute("gameName", game);
        model.addAttribute("listAcc", rows);
        return "customer/ListAcc";
    }
}
