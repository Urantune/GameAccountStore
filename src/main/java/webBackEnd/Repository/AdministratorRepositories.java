package webBackEnd.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.Entity.Administrator;

@Repository
public interface AdministratorRepositories extends JpaRepository<Administrator, Integer> {
}
