package webBackEnd.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

import java.util.Date;

@Entity
@Table(name = "RentAccountGame")
public class RentAccountGame {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rentGameId")
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId",nullable = false, referencedColumnName = "customerId")
    private Customer customer;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameAccountId",referencedColumnName = "gameAccountId",nullable = false)
    private GameAccount gameAccount;
    @Column(name = "dateStart")
    private LocalDateTime dateStart;
    @Column(name = "dateEnd")
    private LocalDateTime dateEnd;
    @Column(name = "deception")
    private String deception;
    @Column(name = "status")
    private String status;

    public RentAccountGame() {
    }

    public RentAccountGame(UUID id, Customer customer, GameAccount gameAccount, LocalDateTime LocalDateTimeStart, LocalDateTime dateEnd, String deception, String status) {
        this.id = id;
        this.customer = customer;
        this.gameAccount = gameAccount;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.deception = deception;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeception() {
        return deception;
    }

    public void setDeception(String deception) {
        this.deception = deception;
    }

    public LocalDateTime getDateStart() {
        return dateStart;
    }

    public void setDateStart(LocalDateTime dateStart) {
        this.dateStart = dateStart;
    }

    public GameAccount getGameAccount() {
        return gameAccount;
    }

    public void setGameAccount(GameAccount gameAccount) {
        this.gameAccount = gameAccount;
    }

    public LocalDateTime getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(LocalDateTime dateEnd) {
        this.dateEnd = dateEnd;
    }
}
