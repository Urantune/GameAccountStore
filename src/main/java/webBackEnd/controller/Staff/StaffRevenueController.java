package webBackEnd.controller.Staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import webBackEnd.entity.Transaction;
import webBackEnd.service.TransactionService;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/staffHome")
public class StaffRevenueController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/revenue")
    public String revenue(Model model) {

        List<Transaction> transactions = transactionService.getAll();
        transactions.sort(Comparator.comparing(Transaction::getDateCreated,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalRefund = BigDecimal.ZERO;

        Map<String, BigDecimal> revenueByDate = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            if (t == null || t.getAmount() == null || t.getDateCreated() == null) continue;

            if (t.getAmount().compareTo(BigDecimal.ZERO) > 0) totalIncome = totalIncome.add(t.getAmount());
            else if (t.getAmount().compareTo(BigDecimal.ZERO) < 0) totalRefund = totalRefund.add(t.getAmount().abs());

            String dateKey = t.getDateCreated().toLocalDate().toString(); // yyyy-MM-dd
            revenueByDate.putIfAbsent(dateKey, BigDecimal.ZERO);
            revenueByDate.put(dateKey, revenueByDate.get(dateKey).add(t.getAmount()));
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalRefund", totalRefund);
        model.addAttribute("netRevenue", totalIncome.subtract(totalRefund));

        // ✅ thêm 2 list để JS ăn thẳng
        model.addAttribute("revenueLabels", new ArrayList<>(revenueByDate.keySet()));
        model.addAttribute("revenueData", revenueByDate.values().stream()
                .map(v -> v == null ? 0.0 : v.doubleValue())
                .toList());

        return "staff/Revenue";
    }

}
