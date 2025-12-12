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
    private UUID walletId;
    @OneToOne
    @JoinColumn(name = "customerId", referencedColumnName = "customerId",nullable = false)
    private Customer users;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "depositDate")
    private LocalDateTime depositDate;
    @Column(name = "dateCreated",nullable = false)
    private LocalDateTime dateCreated;
    @Column(name = "description",nullable = false)
    private String description;

    public Wallet() {
    }

    public Wallet(UUID walletId, Customer users, BigDecimal amount, LocalDateTime depositDate, LocalDateTime dateCreated, String description) {
        this.walletId = walletId;
        this.users = users;
        this.amount = amount;
        this.depositDate = depositDate;
        this.dateCreated = dateCreated;
        this.description = description;
    }

    public UUID getWalletId() {
        return walletId;
    }
    public void setWalletId(UUID walletId) {
        this.walletId = walletId;
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
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}






