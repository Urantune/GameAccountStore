package webBackEnd.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "OrderDetail")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", referencedColumnName = "id")
    private Orders order;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gameId", referencedColumnName = "id")
    private GameAccount gameAccount;
    @Column(name = "endDate")
    private Date endDate;

    public OrderDetail() {
    }

    public OrderDetail(UUID id, Orders order, Date endDate, GameAccount gameAccount) {
        this.id = id;
        this.order = order;
        this.endDate = endDate;
        this.gameAccount = gameAccount;
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

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}

