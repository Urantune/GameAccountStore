package webBackEnd.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.Entity.Voucher;

@Repository
public interface VoucherRepositories extends JpaRepository<Voucher,Integer> {
}
