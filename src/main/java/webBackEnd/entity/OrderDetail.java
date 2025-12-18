package webBackEnd.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "OrderDetail")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "orderDetailId", nullable = false)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", referencedColumnName = "orderId", nullable = false)
    private Orders order;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gameId", referencedColumnName = "gameId")
    private Game game;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gameAccountId", referencedColumnName = "gameAccountId")
    private GameAccount gameAccount;
    @Column(name = "duration", nullable = false)
    private Integer duration;
    private Integer price;

    public OrderDetail() {
    }

    public OrderDetail(UUID id, Orders order, Game game,GameAccount gameAccount, Integer duration, Integer price) {
        this.id = id;
        this.order = order;
        this.game = game;
        this.gameAccount = gameAccount;
        this.duration = duration;
        this.price = price;
    }



    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Orders getOrder() {
        return order;
    }

    public void setOrder(Orders orderId) {
        this.order = orderId;
    }

    public GameAccount getGameAccount() {
        return gameAccount;
    }

    public void setGameAccount(GameAccount gameAccount) {
        this.gameAccount = gameAccount;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}

