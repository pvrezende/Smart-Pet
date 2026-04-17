package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByActiveTrueOrderByNameAsc();
    Optional<Store> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("""
            select s
            from Store s
            where (:active is null or s.active = :active)
              and (
                    :search is null
                    or lower(s.name) like lower(concat('%', :search, '%'))
                    or lower(coalesce(s.code, '')) like lower(concat('%', :search, '%'))
                  )
            """)
    Page<Store> findPageByFilters(
            Boolean active,
            String search,
            Pageable pageable
    );
}