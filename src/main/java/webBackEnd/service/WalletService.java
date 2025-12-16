package webBackEnd.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Transaction;
import webBackEnd.repository.CustomerRepositories;
import webBackEnd.repository.TransactionRepositories;

import java.math.BigDecimal;

@Service
public class WalletService {

    @Autowired
    private CustomerRepositories customerRepo;

    @Autowired
    private TransactionRepositories transactionRepo;

    @Transactional   // ✅ chỉ giữ 1 dòng
    public void topUp(String username, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải > 0");
        }

        Customer c = customerRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy customer"));

        if (c.getBalance() == null) c.setBalance(BigDecimal.ZERO);

        c.setBalance(c.getBalance().add(amount));
        customerRepo.save(c);

        Transaction t = new Transaction();
        t.setCustomer(c);
        t.setAmount(amount);
        t.setDescription("Nạp tiền vào ví");
        t.setDateCreated(java.time.LocalDateTime.now());

        transactionRepo.save(t);
    }
}
