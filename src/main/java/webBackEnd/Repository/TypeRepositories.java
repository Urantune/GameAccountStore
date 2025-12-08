package webBackEnd.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.Entity.Type;
@Repository
public interface TypeRepositories extends JpaRepository<Type,Integer> {
}
