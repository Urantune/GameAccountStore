package webBackEnd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Shift")
public class Shift {

    @Id
    @Column(name = "shiftId", columnDefinition = "uniqueidentifier")
    private UUID shiftId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staffId", nullable = false)
    private Staff staff;

    @Column(name = "dateStart", nullable = false)
    private LocalDateTime dateStart;

    @Column(name = "dateEnd", nullable = false)
    private LocalDateTime dateEnd;

    @Column(name = "timekeeping", length = 100)
    private String timekeeping;

    public UUID getShiftId() {
        return shiftId;
    }

    public void setShiftId(UUID shiftId) {
        this.shiftId = shiftId;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public LocalDateTime getDateStart() {
        return dateStart;
    }

    public void setDateStart(LocalDateTime dateStart) {
        this.dateStart = dateStart;
    }

    public LocalDateTime getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(LocalDateTime dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getTimekeeping() {
        return timekeeping;
    }

    public void setTimekeeping(String timekeeping) {
        this.timekeeping = timekeeping;
    }
}
