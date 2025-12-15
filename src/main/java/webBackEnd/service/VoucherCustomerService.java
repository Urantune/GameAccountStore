package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.repository.VoucherCustomerRepository;

@Service
public class VoucherCustomerService {

    @Autowired
    private VoucherCustomerRepository voucherCustomerRepository;
}
