package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.Customer;
import webBackEnd.repository.CustomerRepositories;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/register")
public class RegisterController {

    @Autowired
    private CustomerRepositories customerRepositories;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public @ResponseBody Map<String, String> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword
    ) {
        Map<String, String> res = new HashMap<>();

        // ===== validate null/empty =====
        if (username == null || email == null || password == null || confirmPassword == null ||
                username.trim().isEmpty() || email.trim().isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            res.put("error", "Please fill all the fields");
            return res;
        }

        username = username.trim();
        email = email.trim();

        // ===== validate username =====
        if (!username.matches("^[a-zA-Z0-9._]+$")) {
            res.put("error", "Invalid username");
            return res;
        }
        if (username.equalsIgnoreCase("Admin")) {
            res.put("error", "Username is already taken");
            return res;
        }
        if (username.startsWith(".") || username.startsWith("_") ||
                username.endsWith(".") || username.endsWith("_")) {
            res.put("error", "Invalid username");
            return res;
        }
        if (username.contains("..") || username.contains("__") ||
                username.contains("._") || username.contains("_.")) {
            res.put("error", "Invalid username");
            return res;
        }
        if (username.length() < 3 || username.length() > 20) {
            res.put("error", "Invalid username");
            return res;
        }

        // ===== validate email =====
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            res.put("error", "Invalid email format");
            return res;
        }

        // ===== validate password =====
        if (password.length() < 8 || password.length() > 16) {
            res.put("error", "Password must be between 8 and 16 characters");
            return res;
        }
        if (!password.equals(confirmPassword)) {
            res.put("error", "Passwords do not match");
            return res;
        }

        // ===== check tồn tại trước =====
        if (customerRepositories.existsByUsername(username)) {
            res.put("error", "Username is already taken");
            return res;
        }
        if (customerRepositories.existsByEmail(email)) {
            res.put("error", "Email is already taken");
            return res;
        }

        // ===== save (nếu lỗi thì KHÔNG lưu gì cả) =====
        Customer user = null;
        try {
            user = new Customer();
            user.setUsername(username);
            user.setEmail(email);
            user.setRole("Customer");
            user.setStatus("ACTIVE");
            user.setPassword(passwordEncoder.encode(password));

            customerRepositories.save(user);

            res.put("success", "Register successfully! Please login.");
            return res;

        } catch (DataIntegrityViolationException ex) {
            // “xóa object” ở đây = bỏ tham chiếu + không save được nên DB không có record mới
            user = null;

            String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
            String lower = (msg == null) ? "" : msg.toLowerCase();

            if (lower.contains("email")) {
                res.put("error", "Email is already taken");
            } else if (lower.contains("username")) {
                res.put("error", "Username is already taken");
            } else {
                res.put("error", "Account already exists");
            }
            return res;

        } catch (Exception ex) {
            user = null;
            res.put("error", "Register failed. Please try again.");
            return res;
        }
    }
}
