package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByActiveTrueOrderByNameAsc();

    Optional<Customer> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    List<Customer> findByActiveOrderByNameAsc(Boolean active);

    List<Customer> findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(String name);

    List<Customer> findByActiveTrueAndCpfContainingOrderByNameAsc(String cpf);

    List<Customer> findByActiveTrueAndPhoneContainingOrderByNameAsc(String phone);

    List<Customer> findByActiveTrueAndEmailContainingIgnoreCaseOrderByNameAsc(String email);
}