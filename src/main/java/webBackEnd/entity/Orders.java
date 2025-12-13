package webBackEnd.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "orderId", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "customerId")
    private Customer customer;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucherId", referencedColumnName = "voucherId")
    private Voucher voucher;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staffId", referencedColumnName = "staffId", nullable = true)
    private Staff staff;
    @Column(name = "totalPrice", nullable = false)
    private BigDecimal totalPrice;
    @Column(name = "orderType", nullable = false)
    private int duration;
    @Column(name = "createdDate", nullable = false)
    private LocalDateTime createdDate;
    @Column(name = "status")
    private String status;


    public Orders() {
    }

    public Orders(UUID id, Customer customer, Voucher voucher, Staff staff, BigDecimal totalPrice, int duration, LocalDateTime createdDate, String status) {
        this.id = id;
        this.customer = customer;
        this.voucher = voucher;
        this.staff = staff;
        this.totalPrice = totalPrice;
        this.duration = duration;
        this.createdDate = createdDate;
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

    public void setCustomer(Customer user) {
        this.customer = user;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int isDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
