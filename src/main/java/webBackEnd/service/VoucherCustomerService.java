package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webBackEnd.entity.VoucherCustomer;
import webBackEnd.repository.VoucherCustomerRepository;

import java.util.List;
import java.util.UUID;

@Service
public class VoucherCustomerService {

    @Autowired
    private VoucherCustomerRepository voucherCustomerRepository;
}
