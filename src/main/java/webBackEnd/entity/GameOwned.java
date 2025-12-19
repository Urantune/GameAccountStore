package webBackEnd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "GameOwned")
public class GameOwned {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "gameOwnedId", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID gameOwnedId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customerId", referencedColumnName = "customerId", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gameAccountId", referencedColumnName = "gameAccountId", nullable = false)
    private GameAccount gameAccount;

    @Column(name = "dateOwned", nullable = false)
    private LocalDateTime dateOwned;

    public GameOwned() {
    }

    public GameOwned(UUID gameOwnedId, Customer customer, GameAccount gameAccount, LocalDateTime dateOwned) {
        this.gameOwnedId = gameOwnedId;
        this.customer = customer;
        this.gameAccount = gameAccount;
        this.dateOwned = dateOwned;
    }

    @PrePersist
    protected void onCreate() {
        if (this.dateOwned == null) this.dateOwned = LocalDateTime.now();
    }

    public UUID getGameOwnedId() {
        return gameOwnedId;
    }

    public void setGameOwnedId(UUID gameOwnedId) {
        this.gameOwnedId = gameOwnedId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public GameAccount getGameAccount() {
        return gameAccount;
    }

    public void setGameAccount(GameAccount gameAccount) {
        this.gameAccount = gameAccount;
    }

    public LocalDateTime getDateOwned() {
        return dateOwned;
    }

    public void setDateOwned(LocalDateTime dateOwned) {
        this.dateOwned = dateOwned;
    }
}
