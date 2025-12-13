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

//    @GetMapping("/staffSelect")
//    public String selectStaff(Model model) {
//
//        model.addAttribute("AOV", "AOV");
//        model.addAttribute("FF", "FF");
//        return "staff/StaffSelect";
//    }

//    @GetMapping("/staffRentalSelect")
//    public String selectStaffRental(Model model) {
//
//        model.addAttribute("AOV", "AOV");
//        model.addAttribute("FF", "FF");
//        return "staff/StaffRentalSelect";
//    }


}
