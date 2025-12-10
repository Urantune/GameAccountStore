package webBackEnd.controller.Admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import webBackEnd.service.CustomerService;

@Controller
@RequestMapping("/adminHome")
public class HomeAdminController {


    @Autowired
    private CustomerService customerService;


    @GetMapping("")
    public String homeAdmin(Model model) {
        return "admin/AdminIndex";
    }

    @GetMapping("/userList")
    public String userList(Model model) {

        model.addAttribute("listUser", customerService.findAllCustomers());
        return "admin/UserList";
    }
}
