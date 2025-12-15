package webBackEnd.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name= "VoucherCustomer")
public class VoucherCustomer {

    @Id
    private UUID voucherUserID;
    @OneToOne
    @JoinColumn(name = "customerId",nullable = false, referencedColumnName = "customerId")
    private Customer customer;
    @OneToOne
    @JoinColumn(name = "voucherId",nullable = false, referencedColumnName = "customerId")
    private Voucher voucher;
    private LocalDateTime dateUsed;

    public VoucherCustomer() {

    }
    public VoucherCustomer(UUID voucherUserID, Customer customer, Voucher voucher) {
        this.voucherUserID = voucherUserID;
        this.customer = customer;
        this.voucher = voucher;
        this.dateUsed = LocalDateTime.now();
    }


    public UUID getVoucherUserID() {
        return voucherUserID;
    }

    public void setVoucherUserID(UUID voucherUserID) {
        this.voucherUserID = voucherUserID;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public LocalDateTime getDateUsed() {
        return dateUsed;
    }

    public void setDateUsed(LocalDateTime dateUsed) {
        this.dateUsed = dateUsed;
    }
}
