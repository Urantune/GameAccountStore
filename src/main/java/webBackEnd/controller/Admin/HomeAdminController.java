package webBackEnd.controller.Admin;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminHome")
public class HomeAdminController {


    @GetMapping("")
    public String homeAdmin() {
        return "admin/AdminIndex";
    }
}
