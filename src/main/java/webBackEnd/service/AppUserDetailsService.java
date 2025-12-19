package webBackEnd.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import webBackEnd.entity.Staff;
import webBackEnd.repository.StaffRepositories;

import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final StaffRepositories staffRepository;
    private final CustomerService customerService;

    public AppUserDetailsService(StaffRepositories staffRepository, CustomerService customerService) {
        this.staffRepository = staffRepository;
        this.customerService = customerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Staff staff = staffRepository.findByUsername(username).orElse(null);
        if (staff != null) {
            if (staff.getStatus() != null && staff.getStatus().equalsIgnoreCase("LOCK")) {
                throw new DisabledException("Staff locked");
            }

            String role = staff.getRole();
            if (role == null || role.isBlank()) role = "STAFF";

            return new org.springframework.security.core.userdetails.User(
                    staff.getUsername(),
                    staff.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            );
        }

        return customerService.loadCustomerByUsername(username);
    }
}
