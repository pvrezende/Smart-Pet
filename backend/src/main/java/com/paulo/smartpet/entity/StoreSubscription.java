package com.paulo.smartpet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "store_subscriptions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_store_subscription_store", columnNames = {"store_id"})
        }
)
public class StoreSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionStatus status;

    private LocalDateTime startsAt;
    private LocalDateTime trialEndsAt;
    private LocalDateTime subscriptionEndsAt;

    @Column(length = 255)
    private String notes;

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

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getTrialEndsAt() {
        return trialEndsAt;
    }

    public LocalDateTime getSubscriptionEndsAt() {
        return subscriptionEndsAt;
    }

    public String getNotes() {
        return notes;
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

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public void setTrialEndsAt(LocalDateTime trialEndsAt) {
        this.trialEndsAt = trialEndsAt;
    }

    public void setSubscriptionEndsAt(LocalDateTime subscriptionEndsAt) {
        this.subscriptionEndsAt = subscriptionEndsAt;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}