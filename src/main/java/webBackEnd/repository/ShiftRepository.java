package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Shift;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    Optional<Shift> findByDateStartAndTimekeeping(LocalDateTime dateStart, String timekeeping);
    List<Shift> findAllByDateStartGreaterThanEqualAndDateStartLessThan(LocalDateTime from, LocalDateTime to);
}
