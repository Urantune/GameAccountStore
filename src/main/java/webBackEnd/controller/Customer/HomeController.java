package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.*;
import webBackEnd.service.*;
import webBackEnd.successfullyDat.PathCheck;
import webBackEnd.successfullyDat.SendMailTest;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private SendMailTest sendMailTest;

    @GetMapping
    public String home(Model model) {
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
        List<GameAccount> listGame = orderDetailService.getAllBoughtAccounts(id);
        model.addAttribute("listGame", gameAccounts);
        model.addAttribute("customer", customer);
        return "customer/ProfileUser";
    }

    @PostMapping("/profile/check-email")
    public String checkEmailAndRedirect(
            @RequestParam UUID userId,
            @RequestParam String email,
            Model model
    ) {
        Customer current = currentUser();

        if (current == null || !current.getCustomerId().equals(userId)) {
            return "redirect:/home/profile/" + userId;
        }

        Customer customer = customerService.findCustomerById(userId);

        if (customer == null) {
            return "redirect:/home/profile/" + userId;
        }

        if (customer.getEmail() == null || !customer.getEmail().equalsIgnoreCase(email.trim())) {
            model.addAttribute("emailError", "Email không khớp.");
            model.addAttribute("openChangePassModal", true);
            model.addAttribute("customer", customer);
            return "customer/ProfileUser";
        }

        LocalDateTime now = LocalDateTime.now();
        int a = now.getHour() * now.getSecond();

        String input = String.valueOf(a) + customer.getCustomerId();
        String fi;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte d : digest) sb.append(String.format("%02x", d));
            fi = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String title = "Đổi mật khẩu của bạn";
        String link = "http://localhost:8080/veryAccount/donePass/" + customer.getCustomerId() + "/" + fi;

        String content =
                "<p>Hãy nhấp vào liên kết dưới đây để thay đổi mật khẩu của bạn:</p>"
                        + "<p><a href=\"" + link + "\">Nhấn vào đây để đổi</a></p>"
                        + "<p>Nếu không bấm được, copy link sau dán vào trình duyệt:<br>" + link + "</p>";

        sendMailTest.testSend(customer.getEmail(), title, content);

        customer.setStatus("CHANGE:" + a);
        customer.setDateUpdated(LocalDateTime.now());
        customerService.save(customer);

        model.addAttribute("customer", customer);
        model.addAttribute("mailSent", true);
        model.addAttribute("mailSentMsg", "✅ Đã gửi email đổi mật khẩu. Vui lòng kiểm tra hộp thư (kể cả Spam).");

        return "customer/ProfileUser";
    }


    @PostMapping(value = "/profile/check-email-ajax", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> checkEmailAjax(@RequestParam UUID userId, @RequestParam String email) {

        Customer current = currentUser();
        if (current == null || !current.getCustomerId().equals(userId)) {
            return Map.of("success", false, "message", "Bạn không có quyền thao tác.");
        }

        Customer customer = customerService.findCustomerById(userId);
        if (customer == null) {
            return Map.of("success", false, "message", "Không tìm thấy tài khoản.");
        }

        if (customer.getEmail() == null || !customer.getEmail().equalsIgnoreCase(email.trim())) {
            return Map.of("success", false, "message", "Email không khớp.");
        }

        LocalDateTime now = LocalDateTime.now();
        int a = now.getHour() * now.getSecond();

        String input = String.valueOf(a) + customer.getCustomerId();
        String fi;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte d : digest) sb.append(String.format("%02x", d));
            fi = sb.toString();
        } catch (Exception e) {
            return Map.of("success", false, "message", "Lỗi tạo mã xác thực.");
        }

        String title = "Đổi mật khẩu của bạn";
        String link = "http://localhost:8080/veryAccount/donePass/" + customer.getCustomerId() + "/" + fi;

        String content =
                "<p>Hãy nhấp vào liên kết dưới đây để thay đổi mật khẩu của bạn:</p>"
                        + "<p><a href=\"" + link + "\">Nhấn vào đây để đổi</a></p>"
                        + "<p>Nếu không bấm được, copy link sau dán vào trình duyệt:<br>" + link + "</p>";

        try {
            sendMailTest.testSend(customer.getEmail(), title, content);
        } catch (Exception e) {
            return Map.of("success", false, "message", "Gửi email thất bại. Vui lòng thử lại.");
        }

        customer.setStatus("CHANGE:" + a);
        customer.setDateUpdated(LocalDateTime.now());
        customerService.save(customer);

        return Map.of(
                "success", true,
                "message", "✅ Đã gửi email đổi mật khẩu. Vui lòng kiểm tra hộp thư (kể cả Spam)."
        );
    }


    @GetMapping("/change-password/{id}")
    public String page(@PathVariable UUID id) {
        Customer c = customerService.findCustomerById(id);

        LocalDateTime random = LocalDateTime.now();
        int a = random.getHour();
        int b = random.getSecond();
        a *= b;

        String input = String.valueOf(a) + c.getCustomerId();
        String fi;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte d : digest) sb.append(String.format("%02x", d));
            fi = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String title = "Đổi mật khẩu của bạn";
        String link = "http://localhost:8080/veryAccount/donePass/" + c.getCustomerId() + "/" + fi;

        String content =
                "<p>Hãy nhấp vào liên kết dưới đây để thay đổi mật khẩu của bạn:</p>"
                        + "<p><a href=\"" + link + "\">Nhấn vào đây để đổi</a></p>"
                        + "<p>Nếu không bấm được, copy link sau dán vào trình duyệt:<br>" + link + "</p>";

        sendMailTest.testSend(c.getEmail(), title, content);

        c.setStatus("CHANGE:" + String.valueOf(a));
        c.setDateUpdated(LocalDateTime.now());
        customerService.save(c);

        return "passchane/change-password";
    }

    @PostMapping(value = "/forgot-password", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> forgotPassword(@RequestParam String email) {
        Customer c = customerService.findByEmail(email.trim());
        if (c == null) {
            return Map.of("error", "Email không tồn tại.");
        }

        LocalDateTime now = LocalDateTime.now();
        int a = now.getHour() * now.getSecond();

        String input = String.valueOf(a) + c.getCustomerId();
        String fi;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte d : digest) sb.append(String.format("%02x", d));
            fi = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String link = "http://localhost:8080/veryAccount/donePass/" + c.getCustomerId() + "/" + fi;

        String content =
                "<p>Bạn đã yêu cầu đổi mật khẩu.</p>"
                        + "<p><a href=\"" + link + "\">Nhấn vào đây để đổi mật khẩu</a></p>";

        sendMailTest.testSend(c.getEmail(), "Đổi mật khẩu", content);

        c.setStatus("CHANGE:" + a);
        c.setDateUpdated(LocalDateTime.now());
        customerService.save(c);

        return Map.of("success", "Đã gửi email đổi mật khẩu.");
    }

    @GetMapping("/transaction")
    public String transaction(
            Model model,
            Principal principal,
            @RequestParam(required = false) String search
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        Customer customer = customerService.findCustomerByUsername(principal.getName());
        List<Transaction> fullHistory = transactionService.getTransactionHistory(customer);
        List<Transaction> transactionHistory = fullHistory;
        List<Transaction> tableHistory = new ArrayList<>(fullHistory);

        if (search != null && !search.isBlank()) {
            String keyword = search.trim().toLowerCase();

            if (keyword.matches("\\d+")) {
                int stt = Integer.parseInt(keyword) - 1;
                tableHistory = (stt >= 0 && stt < fullHistory.size()) ? List.of(fullHistory.get(stt)) : List.of();
            } else if (keyword.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                UUID id = UUID.fromString(keyword);
                tableHistory = fullHistory.stream()
                        .filter(t -> t.getCustomer().equals(id))
                        .toList();
            } else {
                tableHistory = fullHistory.stream()
                        .filter(t -> t.getDescription() != null && t.getDescription().toLowerCase().contains(keyword))
                        .toList();
            }
        }

        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Transaction t : transactionHistory) {
            if (t.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                if(t.getDescription().equalsIgnoreCase("TOPUP")){
                    totalDeposit = totalDeposit.add(t.getAmount());
                }
            } else {
                totalSpent = totalSpent.add(t.getAmount().abs());
            }
        }

        model.addAttribute("balance", customer.getBalance());
        model.addAttribute("totalDeposit", totalDeposit);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("walletHistory", tableHistory);
        model.addAttribute("search", search);

        return "customer/Transaction";
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(
            @RequestParam UUID userId,
            @RequestParam String email,
            Model model
    ) {
        Customer current = currentUser();

        if (current == null || !current.getCustomerId().equals(userId)) {
            return "redirect:/home/profile/" + userId;
        }

        Customer customer = customerService.findCustomerById(userId);

        if (customer != null && customer.getEmail() != null && customer.getEmail().equalsIgnoreCase(email.trim())) {
            customer.setStatus("DELETED");
            customer.setDateUpdated(LocalDateTime.now());
            customerService.save(customer);
            return "redirect:/logout";
        }

        model.addAttribute("deleteError", "Email không khớp.");
        model.addAttribute("openDeleteModal", true);
        model.addAttribute("customer", customer);
        return "customer/ProfileUser";
    }
}
