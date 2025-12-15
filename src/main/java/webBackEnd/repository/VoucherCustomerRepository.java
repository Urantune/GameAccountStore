package webBackEnd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webBackEnd.entity.VoucherCustomer;

import java.util.UUID;

@Repository
public interface VoucherCustomerRepository extends JpaRepository<VoucherCustomer, UUID> {

}
