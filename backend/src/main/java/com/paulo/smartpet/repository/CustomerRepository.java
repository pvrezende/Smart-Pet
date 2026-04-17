package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    @Query("""
            select c
            from Customer c
            where c.store.id = :storeId
              and ((:active is null and c.active = true) or (:active is not null and c.active = :active))
              and (
                    :search is null
                    or lower(c.name) like lower(concat('%', :search, '%'))
                    or lower(coalesce(c.email, '')) like lower(concat('%', :search, '%'))
                    or (:numericSearch is not null and c.cpf like concat('%', :numericSearch, '%'))
                    or (:numericSearch is not null and c.phone like concat('%', :numericSearch, '%'))
                  )
            """)
    Page<Customer> findPageByFilters(
            Long storeId,
            Boolean active,
            String search,
            String numericSearch,
            Pageable pageable
    );
}