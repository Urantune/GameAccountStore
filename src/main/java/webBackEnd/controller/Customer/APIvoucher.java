package webBackEnd.controller.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import webBackEnd.entity.GameAccount;
import webBackEnd.entity.Voucher;
import webBackEnd.service.GameAccountService;
import webBackEnd.service.VoucherService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class APIvoucher {

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private GameAccountService gameAccountService;

    @GetMapping("/api/order/calc")
    public Map<String, Object> calcTotalPrice(
            @RequestParam UUID gameId,
            @RequestParam String packageValues,
            @RequestParam(required = false) String voucherCode
    ) {
        Map<String, Object> result = new HashMap<>();

        GameAccount game = gameAccountService.getGameById(gameId);
        BigDecimal totalPrice = game.getPrice();

        if (packageValues.contains("2 Tháng")) {
            totalPrice = totalPrice.multiply(BigDecimal.valueOf(0.9));
        } else if (packageValues.contains("3 Tháng")) {
            totalPrice = totalPrice.multiply(BigDecimal.valueOf(0.85));
        }

        if (voucherCode != null && !voucherCode.isBlank()) {
            Voucher voucher = voucherService.getValidVoucher(voucherCode);
            if (voucher == null) {
                result.put("error", "Voucher không hợp lệ hoặc đã hết hạn");
                return result;
            }
            BigDecimal discountPercent =
                    BigDecimal.valueOf(voucher.getValue())
                            .divide(BigDecimal.valueOf(100));

            totalPrice = totalPrice.subtract(
                    totalPrice.multiply(discountPercent));
        }

        totalPrice = totalPrice.setScale(0, RoundingMode.HALF_UP);

        result.put("totalPrice", totalPrice);
        return result;
    }

    @GetMapping("/api/voucher/check")
    @ResponseBody
    public Map<String, Object> checkVoucher(@RequestParam String code) {
        Voucher voucher = voucherService.getValidVoucher(code.trim());
        if (voucher == null) {
            return Map.of("valid", false);
        }
        return Map.of(
                "valid", true,
                "percent", voucher.getValue()
        );
    }



}
