package webBackEnd.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "gameId")
    private UUID gameId;
    @Column(name = "gameName")
    private String gameName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "typeId",nullable = false,referencedColumnName = "typeId")
    private Type typeId;
    @Column(name = "dateAddGame")
    private LocalDateTime dateAddGame;

    public Game() {
    }

    public Game(UUID gameId, String gameName, Type typeId, LocalDateTime dateAddGame) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.typeId = typeId;
        this.dateAddGame = dateAddGame;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public Type getTypeId() {
        return typeId;
    }

    public void setTypeId(Type typeId) {
        this.typeId = typeId;
    }

    public LocalDateTime getDateAddGame() {
        return dateAddGame;
    }

    public void setDateAddGame(LocalDateTime dateAddGame) {
        this.dateAddGame = dateAddGame;
    }
}
