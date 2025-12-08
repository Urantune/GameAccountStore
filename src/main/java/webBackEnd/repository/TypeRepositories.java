package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Type;
@Repository
public interface TypeRepositories extends JpaRepository<Type,Integer> {
}
