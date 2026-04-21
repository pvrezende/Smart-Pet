package com.paulo.smartpet.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "saas_audit_logs")
public class SaasAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;

    private Long userId;

    private String username;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    private String action;

    @Column(length = 1000)
    private String details;

    private LocalDateTime createdAt;

    public SaasAuditLog() {
        this.createdAt = LocalDateTime.now();
    }

    public SaasAuditLog(Long storeId, Long userId, String username, UserRole userRole, String action, String details) {
        this.storeId = storeId;
        this.userId = userId;
        this.username = username;
        this.userRole = userRole;
        this.action = action;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }

    // getters
    public Long getId() { return id; }
    public Long getStoreId() { return storeId; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public UserRole getUserRole() { return userRole; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}