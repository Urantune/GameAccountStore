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

    // URL riêng để không đụng HomeController: /home/wallet
    @GetMapping("/wallet")
    public String wallet(Model model,
                         Principal principal,
                         @RequestParam(required = false) String search) {

        if (principal == null) return "redirect:/login";

        Customer c = customerRepo
                .findByUsernameIgnoreCase(principal.getName())
                .orElseThrow();

        BigDecimal balance = (c.getBalance() == null) ? BigDecimal.ZERO : c.getBalance();

        List<Transaction> history;
        if (search != null && !search.isBlank()) {
            history = transactionRepo
                    .findByCustomerAndDescriptionContainingIgnoreCaseOrderByDateCreatedDesc(c, search);
        } else {
            history = transactionRepo
                    .findByCustomerOrderByDateCreatedDesc(c);
        }

        model.addAttribute("balance", balance);
        model.addAttribute("walletHistory", history);

        // nếu bạn CHƯA có sumDeposit/sumSpent trong repo thì tạm tính ở đây
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Transaction t : history) {
            if (t.getAmount() == null) continue;
            if (t.getAmount().compareTo(BigDecimal.ZERO) > 0) totalDeposit = totalDeposit.add(t.getAmount());
            else totalSpent = totalSpent.add(t.getAmount().abs());
        }

        model.addAttribute("totalDeposit", totalDeposit);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("search", search);

        return "customer/Transaction";
    }

    // URL mới: /home/wallet/topup
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
