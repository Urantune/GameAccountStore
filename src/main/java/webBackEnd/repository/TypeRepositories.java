package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Type;

import java.util.UUID;

@Repository
public interface TypeRepositories extends JpaRepository<Type, UUID> {

    Type findByTypeId(UUID id);
}
