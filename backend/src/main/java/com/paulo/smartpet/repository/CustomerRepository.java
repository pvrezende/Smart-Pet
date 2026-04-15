package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByActiveTrueOrderByNameAsc();
    Optional<Customer> findByCpf(String cpf);
}
