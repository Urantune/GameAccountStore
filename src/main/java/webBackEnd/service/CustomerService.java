package webBackEnd.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import webBackEnd.controller.Customer.CustomUserDetails;
import webBackEnd.entity.Customer;
import webBackEnd.repository.CustomerRepositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service()
public class CustomerService implements UserDetailsService {


    @Autowired
    private CustomerRepositories customerRepositories;



    @Override           
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Customer customer = customerRepositories
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        if ("LOCKED".equalsIgnoreCase(customer.getStatus())) {
            throw new DisabledException("Account is locked");
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
    public void save(Customer customer){
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
