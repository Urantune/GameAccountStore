package webBackEnd.service.very;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.Customer;
import webBackEnd.service.CustomerService;

import java.io.IOException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/veryAccount")
public class VerryController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Map<String, Boolean> PAY_STATE = new ConcurrentHashMap<>();



    @GetMapping("/donePass/{id}/{code}")
    public String veryPassAccount(@PathVariable UUID id,
                                  @PathVariable String code,
                                  Model model) {
        Customer customer = customerService.findCustomerById(id);
        if(!customer.getStatus().split(":")[0].equals("CHANGE")){
            return "redirect: customer/home";
        }

        String input = customer.getStatus().split(":")[1] + customer.getCustomerId();
        String fi;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            fi = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(code.equals(fi)){
            return "redirect: customer/home";
        }


        model.addAttribute("customer", customer);
        model.addAttribute("idcustomer", id);
        model.addAttribute("codecustomer", fi);
        return "HTML/ForgotPasword";
    }

    @PostMapping("/donePass")
    public String donePassAccount(@RequestParam UUID id,
                                  @RequestParam String code,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  RedirectAttributes ra) {



        Customer customer = customerService.findCustomerById(id);


        customer.setPassword(passwordEncoder.encode(newPassword));
        customer.setStatus("ACTIVE");
        customerService.save(customer);

        ra.addFlashAttribute("success", "Đổi mật khẩu thành công");
        return "redirect:/welcome";
    }

}
