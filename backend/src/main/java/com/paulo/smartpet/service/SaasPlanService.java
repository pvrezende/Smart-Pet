package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.SaasPlanResponse;
import com.paulo.smartpet.entity.SaasPlan;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.SaasPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SaasPlanService {

    private final SaasPlanRepository saasPlanRepository;

    public SaasPlanService(SaasPlanRepository saasPlanRepository) {
        this.saasPlanRepository = saasPlanRepository;
    }

    public List<SaasPlanResponse> listActive() {
        return saasPlanRepository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SaasPlanResponse> listAll() {
        return saasPlanRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SaasPlanResponse getById(Long id) {
        return toResponse(getEntityById(id));
    }

    public SaasPlan getEntityById(Long id) {
        return saasPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano SaaS não encontrado"));
    }

    @Transactional
    public void ensureDefaultPlansExist() {
        createOrUpdatePlan(
                "BASIC",
                "Plano Basic",
                "Plano inicial para operações enxutas, com recursos essenciais para começar no SaaS.",
                new BigDecimal("99.90"),
                true,
                false,
                1
        );

        createOrUpdatePlan(
                "PRO",
                "Plano Pro",
                "Plano recomendado para operações em crescimento, com mais capacidade e recursos avançados.",
                new BigDecimal("199.90"),
                true,
                true,
                2
        );

        createOrUpdatePlan(
                "ENTERPRISE",
                "Plano Enterprise",
                "Plano completo para operações robustas, com alta capacidade e estrutura para expansão.",
                new BigDecimal("499.90"),
                true,
                false,
                3
        );
    }

    private void createOrUpdatePlan(
            String code,
            String name,
            String description,
            BigDecimal monthlyPrice,
            Boolean active,
            Boolean highlighted,
            Integer displayOrder
    ) {
        SaasPlan saasPlan = saasPlanRepository.findByCode(code)
                .orElseGet(SaasPlan::new);

        saasPlan.setCode(code);
        saasPlan.setName(name);
        saasPlan.setDescription(description);
        saasPlan.setMonthlyPrice(monthlyPrice);
        saasPlan.setActive(active);
        saasPlan.setHighlighted(highlighted);
        saasPlan.setDisplayOrder(displayOrder);

        saasPlanRepository.save(saasPlan);
    }

    private SaasPlanResponse toResponse(SaasPlan saasPlan) {
        return new SaasPlanResponse(
                saasPlan.getId(),
                saasPlan.getCode(),
                saasPlan.getName(),
                saasPlan.getDescription(),
                saasPlan.getMonthlyPrice(),
                saasPlan.getActive(),
                saasPlan.getHighlighted(),
                saasPlan.getDisplayOrder()
        );
    }
}