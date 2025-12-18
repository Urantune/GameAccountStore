package webBackEnd.controller.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.Staff;
import webBackEnd.service.AdministratorService;

import java.util.UUID;

@Controller
@RequestMapping("/adminHome")
public class StaffController {

    @Autowired
    private AdministratorService staffService;

    @GetMapping("/staffList")
    public String staffList(@RequestParam(value = "q", required = false) String q, Model model) {
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("listStaff", staffService.search(q));
        return "admin/StaffList";
    }

    @GetMapping("/staffCreate")
    public String staffCreateForm(Model model) {
        Staff s = new Staff();
        s.setRole("STAFF");
        s.setStatus("ACTIVE");
        model.addAttribute("staff", s);
        return "admin/StaffCreate";
    }

    @PostMapping("/staffCreate")
    public String staffCreateSubmit(@RequestParam("username") String username,
                                    @RequestParam("password") String password,
                                    @RequestParam(value = "status", required = false) String status) {
        staffService.createStaff(username, password, status);
        return "redirect:/adminHome/staffList";
    }

    @GetMapping("/staffUpdate/{id}")
    public String staffUpdateForm(@PathVariable("id") UUID id, Model model) {
        Staff staff = staffService.findById(id).orElseThrow();
        model.addAttribute("staff", staff);
        return "admin/StaffUpdate";
    }

    @PostMapping("/staffUpdate")
    public String staffUpdateSubmit(@RequestParam("id") UUID id,
                                    @RequestParam("username") String username,
                                    @RequestParam(value = "password", required = false) String password,
                                    @RequestParam("status") String status) {
        staffService.updateStaff(id, username, password, status);
        return "redirect:/adminHome/staffList";
    }
}
