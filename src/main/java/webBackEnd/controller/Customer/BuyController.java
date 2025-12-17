package webBackEnd.controller.Customer;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.*;
import webBackEnd.repository.*;
import webBackEnd.service.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping(value = "/home")
public class BuyController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomerRepositories customerRepositories;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrdersRepositories ordersRepositories;
    @Autowired
    private OrderDetailRepositories orderDetailRepositories;
    @Autowired
    private GameAccountService gameAccountService;
    @Autowired
    private GameAccountRepositories gameAccountRepositories;
    @Autowired
    private VoucherService voucherService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private VoucherCustomerRepository voucherCustomerRepository;


    @GetMapping("/payment/{id}")
    public String checkout(@PathVariable("id") UUID id, Model model) {
        GameAccount game = gameAccountService.findGameAccountById(id);
        model.addAttribute("games", game);
        return "customer/Payment";
    }

    @Transactional
    @PostMapping("/order/confirm/{gameId}")
    public String confirmOrder(
            @PathVariable UUID gameId,
            @RequestParam("packageValues") String packageValues,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            Model model
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            model.addAttribute("errorMessage", "Bạn chưa đăng nhập!");
            model.addAttribute("games", getGame(gameId));
            return "customer/Payment";
        }

        Customer customer = customerService.findCustomerByUsername(authentication.getName());
        GameAccount game = getGame(gameId);

        //check acc đã mua
        boolean isOrdered =
                orderDetailRepositories.existsActiveOrderByGameAccount(game.getId());

        if (isOrdered) {
            model.addAttribute("errorMessage",
                    "Tài khoản này đã được đặt hoặc đã bán, không thể tiếp tục đặt hàng!");
            model.addAttribute("games", game);
            return "customer/Payment";
        }

        if (packageValues == null || packageValues.isBlank()) {
            model.addAttribute("errorMessage", "Vui lòng chọn gói thuê");
            model.addAttribute("games", game);
            return "customer/Payment";
        }

        // Tính giá gốc
        BigDecimal totalPrice = game.getPrice();

        if (packageValues.contains("2 Tháng")) {
            totalPrice = totalPrice.multiply(BigDecimal.valueOf(0.9));
        } else if (packageValues.contains("3 Tháng")) {
            totalPrice = totalPrice.multiply(BigDecimal.valueOf(0.85));
        }


        // Voucher
        Voucher voucher = null;
        if (voucherCode != null && !voucherCode.isBlank()) {

            voucher = voucherService.getValidVoucher(voucherCode);
            if (voucher == null) {
                model.addAttribute("errorMessage", "Voucher không hợp lệ hoặc đã hết hạn");
                model.addAttribute("games", game);
                return "customer/Payment";
            }

            //Check voucher đã sd
            boolean used =
                    voucherCustomerRepository.existsByCustomerAndVoucher(customer, voucher);
            if (used) {
                model.addAttribute("errorMessage", "Voucher này đã được sử dụng trước đó");
                model.addAttribute("games", game);
                return "customer/Payment";
            }

            // Áp dụng giảm giá
            BigDecimal discountPercent =
                    BigDecimal.valueOf(voucher.getValue())
                            .divide(BigDecimal.valueOf(100));
            totalPrice = totalPrice.subtract(totalPrice.multiply(discountPercent));
        }

        totalPrice = totalPrice.setScale(0, RoundingMode.HALF_UP);

        // Check tiền
        if (customer.getBalance().compareTo(totalPrice) < 0) {
            model.addAttribute("errorMessage", "Số dư không đủ");
            model.addAttribute("games", game);
            return "customer/Payment";
        }
        // Trừ tiền
        customer.setBalance(customer.getBalance().subtract(totalPrice));
        customerRepositories.save(customer);

        if (voucher != null) {
            VoucherCustomer vc = new VoucherCustomer();
            vc.setCustomer(customer);
            vc.setVoucher(voucher);
            vc.setDateUsed(LocalDateTime.now());

            voucherCustomerRepository.save(vc);
        }

        // Transaction
        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(totalPrice.negate());
        transaction.setDescription("Thanh toán game ID: " + gameId);
        transaction.setDateCreated(LocalDateTime.now());
        transactionService.save(transaction);
        // Orders
        Orders order = new Orders();
        order.setCustomer(customer);
        order.setTotalPrice(totalPrice);
        order.setCreatedDate(LocalDateTime.now());
        order.setStatus("WAIT");
        if (voucher != null) {
            order.setVoucher(voucher);
        }
        Orders savedOrder = ordersRepositories.save(order);
        // OrderDetail
        OrderDetail detail = new OrderDetail();
        detail.setOrder(savedOrder);
        detail.setGameAccount(game);
        detail.setDuration(
                packageValues.contains("1 Tháng") ? 1 :
                        packageValues.contains("2 Tháng") ? 2 :
                                packageValues.contains("3 Tháng") ? 3 : 0
        );
        orderDetailRepositories.save(detail);
        model.addAttribute("successMessage", "Thanh toán thành công!");
        model.addAttribute("games", game);

        return "customer/Payment";
    }

    private GameAccount getGame(UUID gameId) {
        return gameAccountRepositories.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
    }

    @ModelAttribute("currentUser")
    public Customer currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerService.findCustomerByUsername(username);
    }

}
