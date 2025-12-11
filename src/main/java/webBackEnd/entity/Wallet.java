package webBackEnd.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "walletId", columnDefinition = "uniqueidentifier")
    private UUID id;
    @OneToOne
    @JoinColumn(name = "customerId", referencedColumnName = "customerId",nullable = false)
    private Customer users;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "depositDate")
    private LocalDateTime depositDate;
    @Column(name = "dateCreated",nullable = false)
    private LocalDateTime dateCreated;

    public Wallet() {
    }

    public Wallet(UUID id, Customer users, BigDecimal amount, LocalDateTime depositDate, LocalDateTime updatedAt) {
        this.id = id;
        this.users = users;
        this.amount = amount;
        this.depositDate = depositDate;
        this.dateCreated = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Customer getUsers() {
        return users;
    }

    public void setUsers(Customer users) {
        this.users = users;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDepositDate() {
        return depositDate;
    }

    public void setDepositDate(LocalDateTime depositDate) {
        this.depositDate = depositDate;
    }

    public LocalDateTime getUpdatedAt() {
        return dateCreated;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.dateCreated = updatedAt;
    }
}
