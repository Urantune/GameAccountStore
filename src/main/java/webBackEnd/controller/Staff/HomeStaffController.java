package webBackEnd.controller.Staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staffHome")
public class HomeStaffController {

    @GetMapping("")
    public String homeStaff(Model model) {
        return "staff/StaffIndex";
    }

    @GetMapping("/ChangePassword")
    public String changePassword(Model model) {
        return "staff/ChangePassword";
    }
}
