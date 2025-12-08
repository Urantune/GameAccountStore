package webBackEnd.entity;


import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "GameDetail")
public class GameDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "gameId", nullable = false, columnDefinition = "uniqueidentifier")
    private GameAccount gameAccount;
    @Column(name = "imageMain", nullable = false)
    private String mainImage;
    @Column(name = "rank")
    private String rank;
    @Column(name = "skins")
    private String skin;
    @Column(name = "level")
    private String level;
    @Column(name = "items")
    private String items;

    public GameDetail() {
    }

    public GameDetail(UUID id, GameAccount gameAccount, String mainImage,  String rank, String skin, String level, String items) {
        this.id = id;
        this.gameAccount = gameAccount;
        this.mainImage = mainImage;
        this.rank = rank;
        this.skin = skin;
        this.level = level;
        this.items = items;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public GameAccount getGameAccount() {
        return gameAccount;
    }

    public void setGameAccount(GameAccount gameAccount) {
        this.gameAccount = gameAccount;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }
}