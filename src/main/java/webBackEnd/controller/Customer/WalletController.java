package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Transaction;
import webBackEnd.repository.CustomerRepositories;
import webBackEnd.repository.TransactionRepositories;
import webBackEnd.service.CustomerService;
import webBackEnd.service.WalletService;
import webBackEnd.service.very.QrService;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/home")
public class WalletController {

    @Autowired private CustomerRepositories customerRepo;
    @Autowired private TransactionRepositories transactionRepo;
    @Autowired private WalletService walletService;
    @Autowired
    private CustomerService customerService;

    @Autowired private QrService qrService;

    @ModelAttribute("currentUser")
    public Customer currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerService.findCustomerByUsername(username);
    }
    @GetMapping("/wallet")
    public String wallet(Model model, Principal principal,
                         @RequestParam(required = false) String search) {

        if (principal == null) return "redirect:/login";

        Customer c = customerRepo.findByUsernameIgnoreCase(principal.getName()).orElseThrow();
        BigDecimal balance = (c.getBalance() == null) ? BigDecimal.ZERO : c.getBalance();

        List<Transaction> history = (search != null && !search.isBlank())
                ? transactionRepo.findByCustomerAndDescriptionContainingIgnoreCaseOrderByDateCreatedDesc(c, search)
                : transactionRepo.findByCustomerOrderByDateCreatedDesc(c);

        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;
        for (Transaction t : history) {
            if (t.getAmount() == null) continue;
            if (t.getAmount().compareTo(BigDecimal.ZERO) > 0) totalDeposit = totalDeposit.add(t.getAmount());
            else totalSpent = totalSpent.add(t.getAmount().abs());
        }
        model.addAttribute("balance", balance);
        model.addAttribute("walletHistory", history);
        model.addAttribute("totalDeposit", totalDeposit);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("search", search);
        return "customer/Transaction";
    }

    @GetMapping("/wallet/topup")
    public String topUpPage(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Customer c = customerRepo.findByUsernameIgnoreCase(principal.getName()).orElseThrow();
        BigDecimal balance = (c.getBalance() == null) ? BigDecimal.ZERO : c.getBalance();
        model.addAttribute("balance", balance);
        return "customer/TopUp";
    }


    @PostMapping("/wallet/topup")
    public String topUp(@RequestParam BigDecimal amount,
                        Principal principal,
                        Model model) {

        if (principal == null) return "redirect:/login";

        Customer c = customerRepo.findByUsernameIgnoreCase(principal.getName()).orElseThrow();
        BigDecimal balance = (c.getBalance() == null) ? BigDecimal.ZERO : c.getBalance();
        model.addAttribute("balance", balance);


        String idpayment = "TOPUP-" + UUID.randomUUID();

        model.addAttribute("amount", amount);
        model.addAttribute("idpayment", idpayment);


        return "customer/TopUp";
    }


    @GetMapping(value = "/wallet/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> qrPng(@RequestParam("idpayment") String idpayment) {
        byte[] png = qrService.generatePng(idpayment, 280);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }


    @PostMapping("/wallet/topup/confirm")
    public String confirmTopUp(@RequestParam("idpayment") String idpayment,
                               @RequestParam("amount") BigDecimal amount,
                               @AuthenticationPrincipal CustomUserDetails user,
                               RedirectAttributes ra) {

        if (user == null) return "redirect:/login";

        Customer customer = customerRepo.findByUsername(user.getUsername());


        walletService.topUp(customer.getUsername(), amount);

        ra.addFlashAttribute("successPopup",
                "Thanh toán thành công (" + amount.toPlainString() + " đ). Mã: " + idpayment);

        return "redirect:/home/wallet";
    }


}
