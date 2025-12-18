package webBackEnd.controller.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.Staff;
import webBackEnd.entity.Voucher;
import webBackEnd.service.*;
import webBackEnd.successfullyDat.PathCheck;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Controller
@RequestMapping("/adminHome")
public class AdminVoucherController {
    @Autowired
    private CustomerService customerService;

    @Autowired
    private GameAccountService gameAccountService;

    @Autowired
    private GameService gameService;

    @Autowired
    private TypeService typeService;


    @Autowired
    private VoucherService voucherService;

    @Autowired
    private AdministratorService administratorService;


    @Autowired
    private PathCheck pathCheck;



    @GetMapping("/listVoucher")
    public  String listVoucher(Model model) {

        model.addAttribute("listVoucher", voucherService.getAllVoucher());
        return  "admin/VoucherList";
    }

    @GetMapping("/voucherDetail/{id}")
    public String voucherDetail(@PathVariable("id") UUID id, Model model) {

        Voucher voucher = voucherService.getVoucherById(id);


        model.addAttribute("voucher", voucher);

        return "admin/VoucherDetail";
    }

    @GetMapping("/voucherUpdate/{id}")
    public String voucherUpdate(@PathVariable("id") UUID id, Model model) {

        Voucher voucher = voucherService.getVoucherById(id);
        if (voucher == null) {
            throw new RuntimeException("Voucher not found with id: " + id);
        }

        model.addAttribute("voucher", voucher);
        return "admin/VoucherUpdate";
    }

    @PostMapping("/saveVoucher")
    public String saveVoucherUpdate(
            @RequestParam("id") UUID id,
            @RequestParam("voucherName") String voucherName,
            @RequestParam("value") int value,
            @RequestParam("startDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date startDate,
            @RequestParam("endDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date endDate
    ) {

        Voucher existing = voucherService.getVoucherById(id);
        if (existing == null) {
            throw new RuntimeException("Voucher not found with id: " + id);
        }

        existing.setVoucherName(voucherName);
        existing.setValue(value);
        existing.setStartDate(startDate);
        existing.setEndDate(endDate);
        existing.setUpdateDate(LocalDateTime.now());


        voucherService.save(existing);

        return "redirect:/adminHome/listVoucher";
    }

    @GetMapping("/createVoucher")
    public String createVoucher(Model model) {

        Voucher voucher = new Voucher();
        model.addAttribute("voucher", voucher);
        return "admin/CreateVoucher";
    }

    @PostMapping("/saveNewVoucher")
    public String saveNewVoucher(
            @RequestParam String voucherName,
            @RequestParam int value,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        Voucher voucher = new Voucher();
        voucher.setVoucherName(voucherName);
        voucher.setValue(value);
        voucher.setStartDate(startDate);
        voucher.setEndDate(endDate);
        voucher.setUpdateDate(LocalDateTime.now());

        Staff s = administratorService.getStaffByID(UUID.fromString("88A7A905-CB27-431C-BFED-1D16BEA9B91B"));
        voucher.setStaff(s);

        voucherService.save(voucher);

        return "redirect:/adminHome/listVoucher";
    }


    @PostMapping("/deleteVoucher/{id}")
    public String deleteVoucher(@PathVariable UUID id,
                                RedirectAttributes ra) {
        System.out.println(id);
        try {
            voucherService.delete(voucherService.getVoucherById(id));

            ra.addFlashAttribute("success", "Xóa voucher thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa voucher");
        }
        return "redirect:/adminHome/listVoucher";
    }



}
