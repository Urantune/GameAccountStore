package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Customer;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepositories extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByUsernameIgnoreCase(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
