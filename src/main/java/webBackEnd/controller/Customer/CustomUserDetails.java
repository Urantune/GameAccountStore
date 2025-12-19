package webBackEnd.controller.Customer;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import webBackEnd.entity.Customer;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Customer customer;

    public CustomUserDetails(Customer customer) {
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = customer.getRole();
        if (role == null || role.isBlank()) role = "CUSTOMER";
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public String getPassword() {
        return customer.getPassword();
    }

    @Override
    public String getUsername() {
        return customer.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return customer.getStatus() == null || !customer.getStatus().equalsIgnoreCase("LOCKED");
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return customer.getStatus() == null || !customer.getStatus().equalsIgnoreCase("LOCKED");
    }
}
