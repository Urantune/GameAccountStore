package webBackEnd.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cartId", nullable = false)
    private UUID cartId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", referencedColumnName = "customerId",nullable = false)
    private Customer customer;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameAccountId", referencedColumnName = "gameAccountId", nullable = false)
    private GameAccount gameAccount;
    @Column(name = "Datecreate",nullable = false)
    private LocalDateTime createdAt;

    public Cart() {
    }

    public Cart(UUID cartId, Customer customer, GameAccount gameAccount, LocalDateTime createdAt) {
        this.cartId = cartId;
        this.customer = customer;
        this.gameAccount = gameAccount;
        this.createdAt = createdAt;
    }

    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID id) {
        this.cartId = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer user) {
        this.customer = user;
    }

    public GameAccount getGameAccount() {
        return gameAccount;
    }

    public void setGameAccount(GameAccount gameAccount) {
        this.gameAccount = gameAccount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
