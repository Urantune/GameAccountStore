package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Voucher;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoucherRepositories extends JpaRepository<Voucher, UUID> {

    @Query("""
        SELECT v FROM Voucher v
        WHERE v.voucherName = :code
        AND :now BETWEEN v.startDate AND v.endDate
    """)
    Voucher findValidVoucher(@Param("code") String code,
                             @Param("now") Date now);


    Optional<Voucher> findByVoucherNameIgnoreCaseAndStartDateBeforeAndEndDateAfter(
            String voucherName,
            Date startDate,
            Date endDate
    );

    Optional <Voucher> findByVoucherName(String voucherName);

}
