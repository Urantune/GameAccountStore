package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Game;

@Repository
public interface GameRepositories extends JpaRepository<Game,String> {
}
