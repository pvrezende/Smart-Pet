package com.paulo.smartpet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_subscription_billing_history")
public class StoreSubscriptionBillingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private BillingStatus previousBillingStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private BillingStatus newBillingStatus;

    @Column(precision = 10, scale = 2)
    private BigDecimal previousMonthlyPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal newMonthlyPrice;

    private Integer previousBillingDay;
    private Integer newBillingDay;

    private LocalDate previousNextBillingDate;
    private LocalDate newNextBillingDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentProvider paymentProvider;

    @Column(length = 120)
    private String externalCustomerId;

    @Column(length = 120)
    private String externalSubscriptionId;

    @Column(length = 120)
    private String externalBillingId;

    @Column(length = 60)
    private String externalBillingStatus;

    @Column(length = 255)
    private String notes;

    @CreationTimestamp
    private LocalDateTime changedAt;

    public StoreSubscriptionBillingHistory() {
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public BillingStatus getPreviousBillingStatus() {
        return previousBillingStatus;
    }

    public BillingStatus getNewBillingStatus() {
        return newBillingStatus;
    }

    public BigDecimal getPreviousMonthlyPrice() {
        return previousMonthlyPrice;
    }

    public BigDecimal getNewMonthlyPrice() {
        return newMonthlyPrice;
    }

    public Integer getPreviousBillingDay() {
        return previousBillingDay;
    }

    public Integer getNewBillingDay() {
        return newBillingDay;
    }

    public LocalDate getPreviousNextBillingDate() {
        return previousNextBillingDate;
    }

    public LocalDate getNewNextBillingDate() {
        return newNextBillingDate;
    }

    public PaymentProvider getPaymentProvider() {
        return paymentProvider;
    }

    public String getExternalCustomerId() {
        return externalCustomerId;
    }

    public String getExternalSubscriptionId() {
        return externalSubscriptionId;
    }

    public String getExternalBillingId() {
        return externalBillingId;
    }

    public String getExternalBillingStatus() {
        return externalBillingStatus;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public void setPreviousBillingStatus(BillingStatus previousBillingStatus) {
        this.previousBillingStatus = previousBillingStatus;
    }

    public void setNewBillingStatus(BillingStatus newBillingStatus) {
        this.newBillingStatus = newBillingStatus;
    }

    public void setPreviousMonthlyPrice(BigDecimal previousMonthlyPrice) {
        this.previousMonthlyPrice = previousMonthlyPrice;
    }

    public void setNewMonthlyPrice(BigDecimal newMonthlyPrice) {
        this.newMonthlyPrice = newMonthlyPrice;
    }

    public void setPreviousBillingDay(Integer previousBillingDay) {
        this.previousBillingDay = previousBillingDay;
    }

    public void setNewBillingDay(Integer newBillingDay) {
        this.newBillingDay = newBillingDay;
    }

    public void setPreviousNextBillingDate(LocalDate previousNextBillingDate) {
        this.previousNextBillingDate = previousNextBillingDate;
    }

    public void setNewNextBillingDate(LocalDate newNextBillingDate) {
        this.newNextBillingDate = newNextBillingDate;
    }

    public void setPaymentProvider(PaymentProvider paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public void setExternalCustomerId(String externalCustomerId) {
        this.externalCustomerId = externalCustomerId;
    }

    public void setExternalSubscriptionId(String externalSubscriptionId) {
        this.externalSubscriptionId = externalSubscriptionId;
    }

    public void setExternalBillingId(String externalBillingId) {
        this.externalBillingId = externalBillingId;
    }

    public void setExternalBillingStatus(String externalBillingStatus) {
        this.externalBillingStatus = externalBillingStatus;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}