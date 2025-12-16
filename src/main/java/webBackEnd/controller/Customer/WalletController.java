//package webBackEnd.controller.Customer;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//import webBackEnd.entity.Customer;
//import webBackEnd.entity.Transaction;
//import webBackEnd.repository.CustomerRepositories;
//import webBackEnd.repository.TransactionRepositories;
//import webBackEnd.service.WalletService;
//
//import java.math.BigDecimal;
//import java.security.Principal;
//import java.util.List;
//import java.util.UUID;
//
//@Controller
//@RequestMapping("/home")
//public class WalletController {
//
//    @Autowired private CustomerRepositories customerRepo;
//    @Autowired private TransactionRepositories transactionRepo;
//    @Autowired private WalletService walletService;
//
//    // ✅ URL mới để không đụng HomeController: /home/wallet
//    @GetMapping("/wallet")
//    public String wallet(Model model,
//                         Principal principal,
//                         @RequestParam(required = false) String search) {
//
//        if (principal == null) return "redirect:/login";
//
//        String username = principal.getName();
//        Customer c = customerRepo.findByUsernameIgnoreCase(username).orElseThrow();
//
//        BigDecimal balance = (c.getBalance() == null) ? BigDecimal.ZERO : c.getBalance();
//        UUID customerId = c.getCustomerId();
//
//        List<Transaction> history;
//        if (search != null && !search.isBlank()) {
//            // ✅ cần method repo bên dưới
//            history = transactionRepo
//                    .findByCustomerCustomerIdAndDescriptionContainingIgnoreCaseOrderByDepositDateDesc(
//                            customerId, search
//                    );
//        } else {
//            history = transactionRepo
//                    .findByCustomerCustomerIdOrderByDepositDateDesc(customerId);
//        }
//
//        model.addAttribute("balance", balance);
//        model.addAttribute("walletHistory", history);
//        model.addAttribute("totalDeposit", transactionRepo.sumDeposit(customerId));
//        model.addAttribute("totalSpent", transactionRepo.sumSpent(customerId));
//        model.addAttribute("search", search);
//
//        return "customer/Transaction"; // giữ view y như bạn đang dùng
//    }
//
//    // ✅ URL mới: /home/wallet/topup
//    @PostMapping("/wallet/topup")
//    public String topUp(@RequestParam BigDecimal amount,
//                        Principal principal,
//                        RedirectAttributes ra) {
//
//        if (principal == null) return "redirect:/login";
//
//        walletService.topUp(principal.getName(), amount);
//        ra.addFlashAttribute("success", "Nạp tiền thành công: " + amount + " đ");
//
//        // quay về trang wallet mới
//        return "redirect:/home/wallet";
//    }
//}
