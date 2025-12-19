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
    @Column(name = "rank")
    private String rank;
    private int skin;
    @Column(name = "level")
    private int lovel;
    @Column(name = "vip")
    private int vip;
    public OrderDetail() {
    }

    public OrderDetail(Orders order, UUID id, Game game, GameAccount gameAccount, Integer duration, Integer price, String rank, int skin, int lovel, int vip) {
        this.order = order;
        this.id = id;
        this.game = game;
        this.gameAccount = gameAccount;
        this.duration = duration;
        this.price = price;
        this.rank = rank;
        this.skin = skin;
        this.lovel = lovel;
        this.vip = vip;
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

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getLovel() {
        return lovel;
    }

    public void setLovel(int lovel) {
        this.lovel = lovel;
    }

    public int getSkin() {
        return skin;
    }

    public void setSkin(int skin) {
        this.skin = skin;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }
}

