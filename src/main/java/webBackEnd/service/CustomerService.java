package webBackEnd.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import webBackEnd.controller.Customer.CustomUserDetails;
import webBackEnd.entity.Customer;
import webBackEnd.repository.CustomerRepositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepositories customerRepositories;

    public CustomerService(CustomerRepositories customerRepositories) {
        this.customerRepositories = customerRepositories;
    }

    public UserDetails loadCustomerByUsername(String username) {
        Customer customer = customerRepositories
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String st = customer.getStatus();
        if (st != null) {
            if ("BANED".equalsIgnoreCase(st)) {
                throw new LockedException("Tài khoản bị cấm");
            }
            if ("WAITACTIVE".equalsIgnoreCase(st)) {
                throw new DisabledException("Vui lòng kích hoạt tài khoản");
            }
            if ("LOCKED".equalsIgnoreCase(st) || "LOCK".equalsIgnoreCase(st)) {
                throw new LockedException("Tài khoản đã bị khóa");
            }
        }

        return new CustomUserDetails(customer);
    }

    public Customer findByEmail(String email) {
        return customerRepositories.findByEmail(email);
    }

    public List<Customer> findAllCustomers() {
        return customerRepositories.findAll();
    }

    public Customer findCustomerById(UUID customerId) {
        return customerRepositories.findByCustomerId(customerId);
    }

    public void save(Customer customer) {
        customerRepositories.save(customer);
    }

    public Customer findCustomerByUsername(String username) {
        return customerRepositories.findByUsername(username);
    }

    public Customer getCurrentCustomer(String username) {
        return customerRepositories.findByUsername(username);
    }

    public BigDecimal getCustomerBalance(UUID customerId) {
        return customerRepositories.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"))
                .getBalance();
    }

    public void delete(String username) {
        customerRepositories.delete(findCustomerByUsername(username));
    }
}
