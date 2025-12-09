package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Staff;

@Repository
public interface AdministratorRepositories extends JpaRepository<Staff, Integer> {
}
