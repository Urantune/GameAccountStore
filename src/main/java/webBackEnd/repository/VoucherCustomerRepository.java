package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.Customer;
import webBackEnd.entity.Voucher;
import webBackEnd.entity.VoucherCustomer;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherCustomerRepository extends JpaRepository<VoucherCustomer, UUID> {
      boolean existsByCustomerAndVoucher(Customer customer, Voucher voucher);


}
