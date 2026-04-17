package com.paulo.smartpet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    private LocalDateTime saleDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal finalAmount = BigDecimal.ZERO;

    private String paymentMethod;
    private String status = "CONCLUIDA";
    private String notes;

    @Column(length = 50)
    private String source;

    @Column(length = 150)
    private String externalId;

    @Column(length = 30)
    private String fiscalStatus = "PENDENTE";

    @Column(length = 20)
    private String nfeNumber;

    @Column(length = 10)
    private String nfeSeries;

    @Column(length = 60)
    private String nfeAccessKey;

    @Column(length = 20)
    private String nfeEnvironment;

    @Column(length = 500)
    private String nfeErrorMessage;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Store getStore() {
        return store;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public String getSource() {
        return source;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getFiscalStatus() {
        return fiscalStatus;
    }

    public String getNfeNumber() {
        return nfeNumber;
    }

    public String getNfeSeries() {
        return nfeSeries;
    }

    public String getNfeAccessKey() {
        return nfeAccessKey;
    }

    public String getNfeEnvironment() {
        return nfeEnvironment;
    }

    public String getNfeErrorMessage() {
        return nfeErrorMessage;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setFiscalStatus(String fiscalStatus) {
        this.fiscalStatus = fiscalStatus;
    }

    public void setNfeNumber(String nfeNumber) {
        this.nfeNumber = nfeNumber;
    }

    public void setNfeSeries(String nfeSeries) {
        this.nfeSeries = nfeSeries;
    }

    public void setNfeAccessKey(String nfeAccessKey) {
        this.nfeAccessKey = nfeAccessKey;
    }

    public void setNfeEnvironment(String nfeEnvironment) {
        this.nfeEnvironment = nfeEnvironment;
    }

    public void setNfeErrorMessage(String nfeErrorMessage) {
        this.nfeErrorMessage = nfeErrorMessage;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
    }
}