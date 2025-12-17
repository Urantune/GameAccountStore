package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Transaction;
import webBackEnd.repository.CustomerRepositories;
import webBackEnd.repository.TransactionRepositories;
import webBackEnd.service.WalletService;


import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/home")
public class WalletController {

    @Autowired private CustomerRepositories customerRepo;
    @Autowired private TransactionRepositories transactionRepo;
    @Autowired private WalletService walletService;

    // Trang ví: /home/wallet
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

    // ✅ Trang riêng để nạp tiền: /home/wallet/topup
    @GetMapping("/wallet/topup")
    public String topUpPage(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Customer c = customerRepo.findByUsernameIgnoreCase(principal.getName()).orElseThrow();
        BigDecimal balance = (c.getBalance() == null) ? BigDecimal.ZERO : c.getBalance();
        model.addAttribute("balance", balance);

        return "customer/TopUp"; // tạo file TopUp.html
    }

    // ✅ Xử lý nạp tiền
    @PostMapping("/wallet/topup")
    public String topUp(@RequestParam BigDecimal amount,
                        Principal principal,
                        RedirectAttributes ra) {

        if (principal == null) return "redirect:/login";

        walletService.topUp(principal.getName(), amount);
        ra.addFlashAttribute("success", "Nạp tiền thành công: " + amount + " đ");

        return "redirect:/home/wallet";
    }
}
