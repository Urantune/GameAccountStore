package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Voucher;
import webBackEnd.entity.VoucherCustomer;
import webBackEnd.repository.VoucherCustomerRepository;

import java.util.List;
import java.util.UUID;

@Service
public class VoucherCustomerService {

    @Autowired
    private VoucherCustomerRepository voucherCustomerRepository;


    public boolean hasUsed(Customer customer, Voucher voucher) {
        if (customer == null || voucher == null) return false;
        return voucherCustomerRepository.existsByCustomerAndVoucher(customer, voucher);
    }

    public void rollbackUsed(Customer customer, Voucher voucher) {
        if (customer == null || voucher == null) return;
        voucherCustomerRepository.deleteByCustomerAndVoucher(customer, voucher);
    }




}
