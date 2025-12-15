package webBackEnd.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table( name = "Transaction")
public class Transaction {

    /* ================== PRIMARY KEY ================== */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transactionId", columnDefinition = "uniqueidentifier")
    private UUID transactionId;

    /* ================== RELATION ================== */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", nullable = false)
    private Customer customer;

    /* ================== FIELDS ================== */

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "depositDate", nullable = false)
    private LocalDateTime depositDate;

    @Column(name = "dateCreated", nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "description", length = 100, nullable = false)
    private String description;

    /* ================== LIFECYCLE ================== */

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.dateCreated = now;
        this.depositDate = now;
        buildDescription();
    }

    @PreUpdate
    protected void onUpdate() {
        buildDescription();
    }

    /* ================== PRIVATE METHODS ================== */

    private void buildDescription() {
        if (this.amount == null) return;

        if (this.amount.compareTo(BigDecimal.ZERO) > 0) {
            this.description = "Nạp " + formatMoney(this.amount) + " vào ví";
        } else {
            this.description = "Trừ " + formatMoney(this.amount.abs()) + " từ ví";
        }
    }

    private String formatMoney(BigDecimal amount) {
        return String.format("%,.0fđ", amount);
    }

    /* ================== CONSTRUCTORS ================== */

    public Transaction() {
    }

        public Transaction(Customer customer, BigDecimal amount) {
        this.customer = customer;
        this.amount = amount;
    }

    /* ================== GETTERS & SETTERS ================== */

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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
}