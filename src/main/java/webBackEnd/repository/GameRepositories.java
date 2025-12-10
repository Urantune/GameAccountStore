package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Game;
import webBackEnd.entity.GameAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepositories extends JpaRepository<Game,String> {
    Optional<Game> findByGameId(UUID id);



}
