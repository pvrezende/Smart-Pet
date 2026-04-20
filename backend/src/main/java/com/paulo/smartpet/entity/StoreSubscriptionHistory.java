package com.paulo.smartpet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_subscription_history")
public class StoreSubscriptionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SubscriptionPlan previousPlan;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SubscriptionPlan newPlan;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SubscriptionStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SubscriptionStatus newStatus;

    @Column(length = 255)
    private String notes;

    @CreationTimestamp
    private LocalDateTime changedAt;

    public StoreSubscriptionHistory() {
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public SubscriptionPlan getPreviousPlan() {
        return previousPlan;
    }

    public SubscriptionPlan getNewPlan() {
        return newPlan;
    }

    public SubscriptionStatus getPreviousStatus() {
        return previousStatus;
    }

    public SubscriptionStatus getNewStatus() {
        return newStatus;
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

    public void setPreviousPlan(SubscriptionPlan previousPlan) {
        this.previousPlan = previousPlan;
    }

    public void setNewPlan(SubscriptionPlan newPlan) {
        this.newPlan = newPlan;
    }

    public void setPreviousStatus(SubscriptionStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public void setNewStatus(SubscriptionStatus newStatus) {
        this.newStatus = newStatus;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}