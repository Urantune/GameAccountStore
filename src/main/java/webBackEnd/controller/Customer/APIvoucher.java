package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.Voucher;
import webBackEnd.repository.VoucherCustomerRepository;
import webBackEnd.service.GameAccountService;
import webBackEnd.service.VoucherService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
public class APIvoucher {

    @Autowired
    private VoucherService voucherService;
    @GetMapping("/api/voucher/check")
    @ResponseBody
    public Map<String, Object> checkVoucher(@RequestParam String code) {
        Map<String, Object> res = new HashMap<>();
        Optional<Voucher> opt = voucherService.findByVoucherName(code);

        if (opt.isEmpty()) {
            res.put("valid", false);
            return res;
        }
        Voucher v = opt.get();
        Date now = new Date();
        if (now.before(v.getStartDate()) || now.after(v.getEndDate())) {
            res.put("valid", false);
            return res;
        }
        res.put("valid", true);
        res.put("value", v.getValue());
        return res;
    }
}
