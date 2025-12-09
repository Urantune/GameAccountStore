package webBackEnd.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import webBackEnd.config.SecurityConfig;
import webBackEnd.entity.Users;
import webBackEnd.repository.UsersRepositories;


import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/register")
public class RegisterController {
    @Autowired
    private UsersRepositories usersRepositories;
    @Autowired
    private SecurityConfig securityConfig;


    @PostMapping
    public @ResponseBody Map<String, String> register(@RequestParam String username, @RequestParam String email, @RequestParam String password, @RequestParam String confirmPassword) {
        Map<String, String> res = new HashMap<>();
        if (username.isEmpty()  || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
            res.put("error", "Please fill all the fields");
            return res;
        }
        if (!username.matches("^[a-zA-Z0-9._]+$")) {
            res.put("error", "Invalid username");
            return res;
        }
        if(username.equalsIgnoreCase("Admin")){
            res.put("error", "Username is already taken");
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
        if (usersRepositories.existsByUsername(username)) {
            res.put("error", "Username is already taken");
            return res;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            res.put("error", "Invalid email format");
            return res;
        }
        if (usersRepositories.existsByEmail(email)) {
            res.put("error", "Email is already taken");
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

        String hashPassword = securityConfig.passwordEncoder().encode(password);
        Users user = new Users();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(hashPassword);
        usersRepositories.save(user);
        res.put("success", "Register successfully! Please login.");
        return res;
    }
}
