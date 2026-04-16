package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.CompanySettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {
}