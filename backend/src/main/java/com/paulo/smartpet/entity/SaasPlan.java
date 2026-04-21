package com.paulo.smartpet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "saas_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaasPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome técnico (BASIC, PRO, ENTERPRISE)
    @Column(nullable = false, unique = true)
    private String code;

    // Nome exibido para o usuário
    @Column(nullable = false)
    private String name;

    // Descrição comercial
    @Column(length = 1000)
    private String description;

    // Preço mensal
    @Column(nullable = false)
    private BigDecimal monthlyPrice;

    // Indica se o plano está ativo no catálogo
    @Column(nullable = false)
    private Boolean active;

    // Destacar plano (ex: recomendado)
    @Column(nullable = false)
    private Boolean highlighted;

    // Ordem de exibição
    @Column(nullable = false)
    private Integer displayOrder;
}