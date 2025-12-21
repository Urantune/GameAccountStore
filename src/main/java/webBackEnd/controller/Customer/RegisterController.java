package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.Customer;
import webBackEnd.repository.CustomerRepositories;
import webBackEnd.service.very.MailService;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private MailService mailService;

    @Autowired
    private CustomerRepositories customerRepositories;

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
            // ===== TẠO USER (WAITACTIVE) =====
            Customer user = new Customer();
            user.setUsername(username);
            user.setEmail(email);
            user.setRole("Customer");
            user.setStatus("WAITACTIVE");
            user.setPassword(passwordEncoder.encode(password));
            user.setDateCreated(LocalDateTime.now());
            user.setDateUpdated(LocalDateTime.now());
            customerRepositories.save(user);

            // ===== TẠO CODE MD5 (giống flow đổi mật khẩu của bạn) =====
            String input = "WAITACTIVE" + user.getCustomerId();
            String code;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(input.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) sb.append(String.format("%02x", b));
                code = sb.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // ===== LINK KÍCH HOẠT =====
            String verifyLink = "http://localhost:8080/veryAccount/active/" + user.getCustomerId() + "/" + code;

            // ===== EMAIL =====
            String html = """
                    <h2>Xác nhận đăng ký tài khoản</h2>
                    <p>Xin chào <b>%s</b>,</p>
                    <p>Vui lòng nhấn nút bên dưới để kích hoạt tài khoản:</p>
                    <p>
                        <a href="%s"
                           style="padding:12px 24px;
                                  background:#0d6efd;
                                  color:white;
                                  text-decoration:none;
                                  border-radius:6px;
                                  display:inline-block;">
                            Kích hoạt tài khoản
                        </a>
                    </p>
                    <p>Nếu không bấm được, copy link sau dán vào trình duyệt:<br>%s</p>
                    """.formatted(username, verifyLink, verifyLink);

            mailService.sendHtml(email, "Xác nhận đăng ký tài khoản", html);

            res.put("success", "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.");
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            res.put("error", "Register failed. Please try again.");
            return res;
        }
    }
}
