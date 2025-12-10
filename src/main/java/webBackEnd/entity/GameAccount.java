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
    @Column(name = "gameAccountId", columnDefinition = "uniqueidentifier",nullable = false)
    private UUID gameAccountId;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameId", referencedColumnName = "gameId")
    private Game game;
    @Column(name = "imageMain")
    private String imageMain;
    @Column(name = "rank")
    private String rank;
    @Column(name = "skins")
    private int skin;
    @Column(name = "level")
    private int lovel;
    @Column(name = "items")
    private int items;





    public GameAccount() {
    }


    public GameAccount(UUID id, String gameAccount, String gamePassword, BigDecimal price, String description, LocalDateTime createdDate, LocalDateTime duration, LocalDateTime updatedDate, String classify, String status, Game game, String imageMain, String rank, int skin, int lovel, int items) {
        this.gameAccountId = id;
        this.gameAccount = gameAccount;
        this.gamePassword = gamePassword;
        this.price = price;
        this.description = description;
        this.createdDate = createdDate;
        this.duration = duration;
        this.updatedDate = updatedDate;
        this.classify = classify;
        this.status = status;
        this.game = game;
        this.imageMain = imageMain;
        this.rank = rank;
        this.skin = skin;
        this.lovel = lovel;
        this.items = items;
    }

    public UUID getId() {
        return gameAccountId;
    }

    public void setId(UUID id) {
        this.gameAccountId = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
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

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public String getImageMain() {
        return imageMain;
    }

    public void setImageMain(String imageMain) {
        this.imageMain = imageMain;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getSkin() {
        return skin;
    }

    public void setSkin(int skin) {
        this.skin = skin;
    }

    public int getLovel() {
        return lovel;
    }

    public void setLovel(int lovel) {
        this.lovel = lovel;
    }

    public int getItems() {
        return items;
    }

    public void setItems(int items) {
        this.items = items;
    }
}
