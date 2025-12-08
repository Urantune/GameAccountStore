package webBackEnd.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.Entity.Users;

@Repository
public interface UsersRepositories extends JpaRepository<Users,Integer> {
}
