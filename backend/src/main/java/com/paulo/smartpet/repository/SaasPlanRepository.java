package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.SaasPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SaasPlanRepository extends JpaRepository<SaasPlan, Long> {

    Optional<SaasPlan> findByCode(String code);

    List<SaasPlan> findByActiveTrueOrderByDisplayOrderAsc();

    List<SaasPlan> findAllByOrderByDisplayOrderAsc();
}