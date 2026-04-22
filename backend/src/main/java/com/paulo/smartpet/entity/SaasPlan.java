package com.paulo.smartpet.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "saas_plans")
public class SaasPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private BigDecimal monthlyPrice;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private Boolean highlighted;

    @Column(nullable = false)
    private Integer displayOrder;

    public SaasPlan() {
    }

    public SaasPlan(Long id, String code, String name, String description, BigDecimal monthlyPrice, Boolean active, Boolean highlighted, Integer displayOrder) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.monthlyPrice = monthlyPrice;
        this.active = active;
        this.highlighted = highlighted;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean getHighlighted() {
        return highlighted;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setHighlighted(Boolean highlighted) {
        this.highlighted = highlighted;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}