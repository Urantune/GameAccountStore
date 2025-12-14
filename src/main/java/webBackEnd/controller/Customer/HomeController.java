package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.*;
import webBackEnd.service.*;
import webBackEnd.successfullyDat.PathCheck;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(value = "/home")
public class HomeController {


    @Autowired
    private TransactionService transactionService;

    @Autowired
    private GameAccountService gameAccountService;

    @Autowired
    private PathCheck pathCheck;
    @Autowired
    private GameService gameService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;

    @GetMapping
    public String home(Model model) {
//        for(GameAccount a : gameAccountService.get20Profuct()){
//            System.out.println(pathCheck.getPathWithOS(gameDetailService.getGameDetailByGameAccount(a).getMainImage()));
//        }
        model.addAttribute("list20Product", gameAccountService.get20Profuct());
        List<Game> game = gameService.findAllGame();
        model.addAttribute("game", game);

        return "customer/index";
    }


    @ModelAttribute("currentUser")
    public Customer currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerService.findCustomerByUsername(username);
    }


    @GetMapping("/profile/{id}")
    public String profile(Model model, @PathVariable("id") UUID id) {
        Customer customer = customerService.findCustomerById(id);
        List<Orders> order = ordersService.findAllByStatus("COMPLETED");
        List<GameAccount> gameAccounts = new ArrayList<>();
        for (Orders o : order) {
            for (OrderDetail e : orderDetailService.findAllByOrderId(o.getId())) {
                gameAccounts.add(e.getGameAccount());
            }
        }
        List<GameAccount> listGame =
                orderDetailService.getAllBoughtAccounts(id);
        model.addAttribute("listGame", gameAccounts);
        model.addAttribute("customer", customer);
        return "customer/ProfileUser";
    }

    @GetMapping("/transaction")
    public String transaction(
            Model model,
            Principal principal,
            @RequestParam(required = false) String search) {

        if (principal == null) {
            return "redirect:/login";
        }

        Customer customer =
                customerService.findCustomerByUsername(principal.getName());

        // 1️⃣ Lấy toàn bộ transaction (đảm bảo không null)
        List<Transaction> allWallets = transactionService.getAll();
        if (allWallets == null) {
            allWallets = List.of();
        }

        // 2️⃣ Lọc theo customer + search + sort
        List<Transaction> walletHistory = allWallets.stream()
                .filter(w -> w.getCustomer() != null)
                .filter(w -> w.getCustomer().getCustomerId()
                        .equals(customer.getCustomerId()))
                .filter(w -> search == null
                        || search.isBlank()
                        || (w.getDescription() != null
                        && w.getDescription().toLowerCase()
                        .contains(search.toLowerCase())))
                .sorted((a, b) -> b.getDepositDate()
                        .compareTo(a.getDepositDate()))
                .toList();

        // 3️⃣ Tính toán tiền (BigDecimal cho CHUẨN)
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Transaction w : walletHistory) {
            if (w.getAmount() == null) continue;

            if (w.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalDeposit = totalDeposit.add(w.getAmount());
            } else {
                totalSpent = totalSpent.add(w.getAmount().abs());
            }
        }

        model.addAttribute("balance", customer.getBalance());
        model.addAttribute("totalDeposit", totalDeposit);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("walletHistory", walletHistory);
        model.addAttribute("search", search);

        return "customer/Transaction";
    }


}
