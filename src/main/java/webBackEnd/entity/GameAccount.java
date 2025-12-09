package webBackEnd.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "GameAccount")
public class GameAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier",nullable = false)
    private UUID id;
    @Column(name = "gameName",nullable = false)
    private String gameName;
    @Column(name = "gameUserName",nullable = false)
    private String gameAccount;
    @Column(name = "gamePassword",nullable = false)
    private String gamePassword;
    @Column(name = "price",nullable = false)
    private BigDecimal price;
    @Column(name = "description", columnDefinition = "VARCHAR(MAX)")
    private String description;
    @Column(name = "created",nullable = false)
    private LocalDateTime createdDate;
    @Column(name = "updated")
    private LocalDateTime updatedDate;
    @Column(name = "durationAccount")
    private LocalDateTime duration;
    @Column(name = "status")
    private String status;
    @Column(name = "classify")
    private String classify;

    public GameAccount() {
    }

    public GameAccount(UUID id, String gameName, String gameAccount, String gamePassword, BigDecimal price, String description, LocalDateTime createdDate, LocalDateTime updatedDate, LocalDateTime duration, String status, String classify) {
        this.id = id;
        this.gameName = gameName;
        this.gameAccount = gameAccount;
        this.gamePassword = gamePassword;
        this.price = price;
        this.description = description;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.duration = duration;
        this.status = status;
        this.classify = classify;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameAccount() {
        return gameAccount;
    }

    public void setGameAccount(String gameAccount) {
        this.gameAccount = gameAccount;
    }

    public String getGamePassword() {
        return gamePassword;
    }

    public void setGamePassword(String gamePassword) {
        this.gamePassword = gamePassword;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public LocalDateTime getDuration() {
        return duration;
    }

    public void setDuration(LocalDateTime duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getClassify() {
        return classify;
    }
    public void setClassify(String classify) {
        this.classify = classify;
    }
}
