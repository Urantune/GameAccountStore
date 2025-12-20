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
    @Column(name = "rank")
    private String rank;
    @Column(name = "skin")
    private int skin;
    @Column(name = "level")
    private int lovel;
    @Column(name = "vip")
    private int vip;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameAccountId", nullable = false)
    private GameAccount gameAccount;
    public Cart() {
    }


    public Cart(UUID cartId, GameAccount gameAccount, int vip, int lovel, int skin, String rank, BigDecimal price, Integer duration, LocalDateTime createdAt, Game game, Customer customer) {
        this.cartId = cartId;
        this.gameAccount = gameAccount;
        this.vip = vip;
        this.lovel = lovel;
        this.skin = skin;
        this.rank = rank;
        this.price = price;
        this.duration = duration;
        this.createdAt = createdAt;
        this.game = game;
        this.customer = customer;
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

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public GameAccount getGameAccount() {
        return gameAccount;
    }

    public void setGameAccount(GameAccount gameAccount) {
        this.gameAccount = gameAccount;
    }
}
