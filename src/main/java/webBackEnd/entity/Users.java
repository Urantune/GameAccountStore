package webBackEnd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID id;

    @Column(name = "userName", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "dateCreated", nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "dateUpdated", nullable = false)
    private LocalDateTime dateUpdated;

    @Column(name = "status")
    private String status;

    @Column(name = "role",nullable = false)
    private String role;

    public Users() {
    }

    public Users(UUID id, String username, String email, String password, LocalDateTime dateCreated, LocalDateTime dateUpdated, String status, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.status = status;
        this.role = role;
    }

    // Hàm tự set ngày khi tạo mới
    @PrePersist
    protected void onCreate() {
        this.dateCreated = LocalDateTime.now();
        this.dateUpdated = LocalDateTime.now();
    }

    // Hàm tự cập nhật ngày khi update
    @PreUpdate
    protected void onUpdate() {
        this.dateUpdated = LocalDateTime.now();
    }

    // Getter - Setter
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
