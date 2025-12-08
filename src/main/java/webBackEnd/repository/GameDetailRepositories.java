package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.GameDetail;

@Repository
public interface GameDetailRepositories extends JpaRepository<GameDetail,Integer> {
}
