package webBackEnd.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "Type")
public class Type {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameId", referencedColumnName = "id")
    private GameAccount gameAccount;
    @Column(name = "typeName", nullable = false)
    private String typeName;
    @Column(name = "description")
    private String description;
    @Column(name = "status", nullable = false)
    private String status;

    public Type() {
    }

    public Type(UUID id, String status, String description, String categoryName, GameAccount gameAccount) {
        this.id = id;
        this.status = status;
        this.description = description;
        this.typeName = typeName;
        this.gameAccount = gameAccount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String categoryName) {
        this.typeName = categoryName;
    }

    public GameAccount getGameAccount() {
        return gameAccount;
    }

    public void setGameAccount(GameAccount gameAccount) {
        this.gameAccount = gameAccount;
    }
}
