package com.paulo.smartpet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_subscriptions")
public class StoreSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BillingStatus billingStatus;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    private LocalDateTime trialEndsAt;

    private LocalDateTime subscriptionEndsAt;

    @Column(nullable = false)
    private Integer billingDay;

    private LocalDate nextBillingDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(length = 255)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentProvider paymentProvider;

    @Column(length = 120)
    private String externalSubscriptionId;

    @Column(length = 120)
    private String externalBillingId;

    @Column(length = 60)
    private String externalBillingStatus;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public StoreSubscription() {
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public BillingStatus getBillingStatus() {
        return billingStatus;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getTrialEndsAt() {
        return trialEndsAt;
    }

    public LocalDateTime getSubscriptionEndsAt() {
        return subscriptionEndsAt;
    }

    public Integer getBillingDay() {
        return billingDay;
    }

    public LocalDate getNextBillingDate() {
        return nextBillingDate;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public String getNotes() {
        return notes;
    }

    public PaymentProvider getPaymentProvider() {
        return paymentProvider;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public void setBillingStatus(BillingStatus billingStatus) {
        this.billingStatus = billingStatus;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public void setTrialEndsAt(LocalDateTime trialEndsAt) {
        this.trialEndsAt = trialEndsAt;
    }

    public void setSubscriptionEndsAt(LocalDateTime subscriptionEndsAt) {
        this.subscriptionEndsAt = subscriptionEndsAt;
    }

    public void setBillingDay(Integer billingDay) {
        this.billingDay = billingDay;
    }

    public void setNextBillingDate(LocalDate nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setPaymentProvider(PaymentProvider paymentProvider) {
        this.paymentProvider = paymentProvider;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}