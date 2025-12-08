package webBackEnd.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier")
    private UUID id;
    @OneToOne
    @JoinColumn(name = "userId", referencedColumnName = "id",nullable = false)
    private Users users;
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "depositDate")
    private LocalDateTime depositDate;
    @Column(name = "dateCreated",nullable = false)
    private LocalDateTime dateCreated;

    public Wallet() {
    }

    public Wallet(UUID id, Users users, BigDecimal balance, BigDecimal amount, LocalDateTime depositDate, LocalDateTime updatedAt) {
        this.id = id;
        this.users = users;
        this.balance = balance;
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

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
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
