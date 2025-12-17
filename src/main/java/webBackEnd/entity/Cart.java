package webBackEnd.entity;

import jakarta.persistence.*;
import org.apache.poi.hpsf.Decimal;

import java.math.BigDecimal;
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
    @JoinColumn(name = "gameId", referencedColumnName = "gameId", nullable = false)
    private Game game;
    @Column(name = "Datecreate",nullable = false)
    private LocalDateTime createdAt;
    private BigDecimal price;
    private Integer duration;

    public Cart() {
    }

    public Cart(UUID cartId, Customer customer, Game gAme, LocalDateTime createdAt,Integer duration) {
        this.cartId = cartId;
        this.customer = customer;
        this.game = game;
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

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
