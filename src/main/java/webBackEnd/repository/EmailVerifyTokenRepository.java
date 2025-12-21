package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.EmailVerifyToken;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerifyTokenRepository
        extends JpaRepository<EmailVerifyToken, UUID> {

    Optional<EmailVerifyToken> findByToken(String token);
}
