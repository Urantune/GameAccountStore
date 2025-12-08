package webBackEnd.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "Voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier")
    private UUID id;
    @Column(name = "voucherName",nullable = false)
    private String voucherName;
    @Column(name = "value",nullable = false)
    private int value;
    @Column(name = "startDate",nullable = false)
    private Date startDate;
    @Column(name = "endDate",nullable = false)
    private Date endDate;
    @Column(name = "dateUpdate")
    private LocalDateTime updateDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adminId", referencedColumnName = "id",nullable = false)
    private Administrator administrator;

    public Voucher() {
    }

    public Voucher(UUID id, String voucherName, int value, Date startDate, Date endDate, LocalDateTime updateDate, Administrator administrator) {
        this.id = id;
        this.voucherName = voucherName;
        this.value = value;
        this.startDate = startDate;
        this.endDate = endDate;
        this.updateDate = updateDate;
        this.administrator = administrator;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getVoucherName() {
        return voucherName;
    }

    public void setVoucherName(String voucherName) {
        this.voucherName = voucherName;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public Administrator getAdministrator() {
        return administrator;
    }

    public void setAdministrator(Administrator administrator) {
        this.administrator = administrator;
    }
}
