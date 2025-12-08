package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Administrator;

@Repository
public interface AdministratorRepositories extends JpaRepository<Administrator, Integer> {
}
