package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Users;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepositories extends JpaRepository<Users, UUID> {
    Users getUserByUsernameIgnoreCase(String username);
    Optional<Users> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
