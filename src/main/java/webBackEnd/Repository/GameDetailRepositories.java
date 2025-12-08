package webBackEnd.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.Entity.GameDetail;

@Repository
public interface GameDetailRepositories extends JpaRepository<GameDetail,Integer> {
}
