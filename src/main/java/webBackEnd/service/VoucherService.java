package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Voucher;
import webBackEnd.repository.VoucherRepositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
public class VoucherService {

    @Autowired
    private VoucherRepositories voucherRepositories;

    public List<Voucher> getAllVoucher(){
        return voucherRepositories.findAll();
    }

    public Voucher getVoucherById(UUID id){
        return voucherRepositories.findById(id).get();
    }

    public Voucher save(Voucher voucher){
        return  voucherRepositories.save(voucher);
    }


    public void delete(Voucher voucher){
        voucherRepositories.delete(voucher);
    }

    public void deleteById(UUID id) {
        Voucher voucher = voucherRepositories.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        voucherRepositories.delete(voucher);
    }


    public Voucher getValidVoucher(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return voucherRepositories.findValidVoucher(code.trim(), new Date());
    }

    public Voucher getValidVouchers(String code) {
        Date now = new Date();

        return voucherRepositories.findByVoucherNameIgnoreCaseAndStartDateBeforeAndEndDateAfter(code, now, now).orElse(null);
    }


     public Optional<Voucher> findByVoucherName(String voucherName) {
        return voucherRepositories.findByVoucherName(voucherName);
    }

}
