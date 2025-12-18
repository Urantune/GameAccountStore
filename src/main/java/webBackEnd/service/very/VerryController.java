package webBackEnd.service.very;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.Customer;
import webBackEnd.service.CustomerService;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/veryAccount")
public class VerryController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/donePass/{id}/{code}")
    public String veryPassAccount(@PathVariable UUID id,
                                  @PathVariable String code,
                                  org.springframework.ui.Model model) {

        Customer customer = customerService.findCustomerById(id);
        if (customer == null || customer.getStatus() == null) return "redirect:/home";

        String[] parts = customer.getStatus().split(":");
        if (parts.length != 2 || !"CHANGE".equals(parts[0])) return "redirect:/home";

        String input = parts[1] + customer.getCustomerId();
        String fi;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            fi = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!code.equals(fi)) return "redirect:/home";

        model.addAttribute("idcustomer", id);
        model.addAttribute("codecustomer", fi);
        model.addAttribute("customer", customer);
        return "passchane/ChangePassword";
    }

    @PostMapping("/donePass")
    public String donePassAccount(
            @RequestParam UUID id,
            @RequestParam String code,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes ra
    ) {

        Customer customer = customerService.findCustomerById(id);
        if (customer == null || customer.getStatus() == null) {
            ra.addFlashAttribute("errorMessage", "Link không hợp lệ.");
            return "redirect:/home";
        }

        if (newPassword == null || newPassword.isBlank() || !newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
            return "redirect:/veryAccount/donePass/" + id + "/" + code;
        }

        String[] parts = customer.getStatus().split(":");
        if (parts.length != 2 || !"CHANGE".equals(parts[0])) {
            ra.addFlashAttribute("errorMessage", "Yêu cầu đổi mật khẩu đã hết hạn.");
            return "redirect:/home";
        }

        String input = parts[1] + customer.getCustomerId();
        String fi;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            fi = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!code.equals(fi)) {
            ra.addFlashAttribute("errorMessage", "Link không hợp lệ.");
            return "redirect:/home";
        }

        customer.setPassword(passwordEncoder.encode(newPassword));
        customer.setStatus("ACTIVE");
        customer.setDateUpdated(LocalDateTime.now());
        customerService.save(customer);

        ra.addFlashAttribute("successMessage", "Đổi mật khẩu thành công.");
        return "redirect:/home";
    }
}
