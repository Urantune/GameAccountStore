package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Users;

@Repository
public interface UsersRepositories extends JpaRepository<Users,Integer> {
}
