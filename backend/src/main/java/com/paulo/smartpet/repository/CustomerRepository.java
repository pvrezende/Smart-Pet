package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByStoreIdAndActiveTrueOrderByNameAsc(Long storeId);

    List<Customer> findByStoreIdAndActiveOrderByNameAsc(Long storeId, Boolean active);

    Optional<Customer> findByStoreIdAndCpf(Long storeId, String cpf);

    boolean existsByStoreIdAndCpf(Long storeId, String cpf);

    boolean existsByStoreIdAndCpfAndIdNot(Long storeId, String cpf, Long id);

    List<Customer> findByStoreIdAndActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(Long storeId, String name);

    List<Customer> findByStoreIdAndActiveTrueAndCpfContainingOrderByNameAsc(Long storeId, String cpf);

    List<Customer> findByStoreIdAndActiveTrueAndPhoneContainingOrderByNameAsc(Long storeId, String phone);

    List<Customer> findByStoreIdAndActiveTrueAndEmailContainingIgnoreCaseOrderByNameAsc(Long storeId, String email);
}