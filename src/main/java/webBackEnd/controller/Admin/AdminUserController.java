package webBackEnd.controller.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.Customer;
import webBackEnd.service.*;
import webBackEnd.successfullyDat.PathCheck;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/adminHome")
public class AdminUserController {

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

}
