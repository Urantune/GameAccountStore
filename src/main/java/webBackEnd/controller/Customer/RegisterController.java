package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.Customer;
import webBackEnd.entity.EmailVerifyToken;
import webBackEnd.repository.CustomerRepositories;
import webBackEnd.repository.EmailVerifyTokenRepository;
import webBackEnd.service.very.MailService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping(value = "/register")
public class RegisterController {
    @Autowired
    private MailService mailService;
    @Autowired
    private CustomerRepositories customerRepositories;
    @Autowired
    private EmailVerifyTokenRepository emailVerifyTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    @ResponseBody
    @Transactional
    public Map<String, String> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword
    ) {
        Map<String, String> res = new HashMap<>();

        // ===== validate =====
        if (username == null || email == null || password == null || confirmPassword == null ||
                username.trim().isEmpty() || email.trim().isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
            res.put("error", "Please fill all the fields");
            return res;
        }

        username = username.trim();
        email = email.trim();

        if (!username.matches("^[a-zA-Z0-9._]{3,20}$")
                || username.equalsIgnoreCase("Admin")
                || username.startsWith(".") || username.startsWith("_")
                || username.endsWith(".") || username.endsWith("_")
                || username.contains("..") || username.contains("__")
                || username.contains("._") || username.contains("_.")
        ) {
            res.put("error", "Invalid username");
            return res;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            res.put("error", "Invalid email format");
            return res;
        }

        if (password.length() < 8 || password.length() > 16) {
            res.put("error", "Password must be between 8 and 16 characters");
            return res;
        }

        if (!password.equals(confirmPassword)) {
            res.put("error", "Passwords do not match");
            return res;
        }

        if (customerRepositories.existsByUsername(username)) {
            res.put("error", "Username is already taken");
            return res;
        }

        if (customerRepositories.existsByEmail(email)) {
            res.put("error", "Email is already taken");
            return res;
        }

        try {
            // ===== TẠO USER (INACTIVE) =====
            Customer user = new Customer();
            user.setUsername(username);
            user.setEmail(email);
            user.setRole("Customer");
            user.setStatus("INACTIVE");
            user.setPassword(passwordEncoder.encode(password));
            customerRepositories.save(user);

            // ===== TOKEN =====
            String token = UUID.randomUUID().toString();

            EmailVerifyToken vt = new EmailVerifyToken();
            vt.setToken(token);
            vt.setCustomer(user);
            vt.setExpiredAt(LocalDateTime.now().plusHours(24));
            vt.setUsed(false);
            emailVerifyTokenRepository.save(vt);

            // ===== EMAIL =====
            String verifyLink = "http://localhost:8080/verify-email?token=" + token;

            String html = """
                    <h2>Xác nhận đăng ký tài khoản</h2>
                    <p>Xin chào <b>%s</b>,</p>
                    <p>Vui lòng nhấn nút bên dưới để xác nhận email:</p>
                    <p>
                        <a href="%s"
                           style="padding:12px 24px;
                                  background:#0d6efd;
                                  color:white;
                                  text-decoration:none;
                                  border-radius:6px;
                                  display:inline-block;">
                            Xác nhận email
                        </a>
                    </p>
                    <p>Link có hiệu lực trong 24 giờ.</p>
                    """.formatted(username, verifyLink);

            mailService.sendHtml(email, "Xác nhận đăng ký tài khoản", html);

            res.put("success", "Đăng ký thành công! Vui lòng kiểm tra email để xác nhận.");
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            res.put("error", "Register failed. Please try again.");
            return res;
        }
    }

    @GetMapping("/verify-email")
    @Transactional
    public String verifyEmail(@RequestParam String token, Model model) {

        Optional<EmailVerifyToken> opt = emailVerifyTokenRepository.findByToken(token);

        if (opt.isEmpty()) {
            model.addAttribute("error", "Link không hợp lệ");
            return "verify-result";
        }

        EmailVerifyToken vt = opt.get();

        if (vt.isUsed() || vt.getExpiredAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Link đã hết hạn hoặc đã sử dụng");
            return "verify-result";
        }
        Customer customer = vt.getCustomer();
        customer.setStatus("ACTIVE");
        customerRepositories.save(customer);
        vt.setUsed(true);
        emailVerifyTokenRepository.save(vt);
        model.addAttribute("success", "Xác nhận thành công! Bạn có thể đăng nhập.");
        return "redirect:/home";
    }
}
