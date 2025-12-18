package webBackEnd.controller.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import webBackEnd.entity.GameAccount;
import webBackEnd.service.GameAccountService;

import java.util.List;

@Controller
@RequestMapping("/adminHome")
public class ManageExpired {

    @Autowired
    private GameAccountService gameAccountService;

    @GetMapping("/manageExpired")
    public String manageExpired(Model model) {
        List<GameAccount> expiredList = gameAccountService.findExpired();
        model.addAttribute("expiredList", expiredList);
        return "admin/ManageExpired";
    }
}
