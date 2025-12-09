package webBackEnd.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/redirectByRole")
    public String redirectByRole(Authentication auth) {
        boolean isAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return "redirect:/home/cart";
        } else {
            return "redirect:/home";
        }
    }
}
