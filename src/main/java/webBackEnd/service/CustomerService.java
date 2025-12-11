package webBackEnd.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Customer;
import webBackEnd.repository.CustomerRepositories;

import java.util.List;
import java.util.UUID;

@Service()
public class CustomerService implements UserDetailsService {
    @Autowired
    private CustomerRepositories customerRepositories;
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer user = customerRepositories.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
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

}
