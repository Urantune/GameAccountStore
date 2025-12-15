package webBackEnd.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "Transaction")
public class Transaction {

    @Id
    @Column(name = "transactionId")
    @GeneratedValue
    private UUID transactionId;

    @ManyToOne
    @JoinColumn(name = "customerId")
    private Customer customer;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Column(name = "dateCreated")
    private LocalDateTime dateCreated;

    public Transaction() {
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Transaction(UUID transactionId, Customer customer, BigDecimal amount, String description, LocalDateTime dateCreated) {
        this.transactionId = transactionId;
        this.customer = customer;
        this.amount = amount;
        this.description = description;
        this.dateCreated = dateCreated;
    }
}
