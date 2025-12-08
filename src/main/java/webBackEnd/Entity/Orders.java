package webBackEnd.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id",columnDefinition = "uniqueidentifier",nullable = false)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "id",nullable = false)
    private Users user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucherId", referencedColumnName = "id")
    private Voucher voucher;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staffId",referencedColumnName = "id",nullable = false)
    private Administrator staff;
    @Column(name = "totalPrice",nullable = false)
    private BigDecimal totalPrice;
    @Column(name = "orderType",nullable = false)
    private String type;
    @Column(name = "createdDate",nullable = false)
    private LocalDateTime createdDate;
    @Column(name = "status")
    private String status;


    public Orders() {
    }

    public Orders(UUID id, Users user, Voucher voucher, Administrator staff, BigDecimal totalPrice, String type, LocalDateTime createdDate, String status) {
        this.id = id;
        this.user = user;
        this.voucher = voucher;
        this.staff = staff;
        this.totalPrice = totalPrice;
        this.type = type;
        this.createdDate = createdDate;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public Administrator getStaff() {
        return staff;
    }

    public void setStaff(Administrator staff) {
        this.staff = staff;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
