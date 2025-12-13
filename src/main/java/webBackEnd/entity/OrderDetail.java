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
    @JoinColumn(name = "gameId", referencedColumnName = "gameAccountId")
    private GameAccount gameAccount;
    @Column(name = "duration", nullable = false)
    private int duration;

    public OrderDetail() {
    }

    public OrderDetail(UUID id, Orders order, GameAccount gameAccount, int duration) {
        this.id = id;
        this.order = order;
        this.gameAccount = gameAccount;
        this.duration = duration;
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


    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}

