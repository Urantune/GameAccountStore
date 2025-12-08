package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Voucher;

@Repository
public interface VoucherRepositories extends JpaRepository<Voucher,Integer> {
}
