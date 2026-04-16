package com.paulo.smartpet.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "company_settings")
public class CompanySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150, nullable = false)
    private String tradeName;

    @Column(length = 200)
    private String legalName;

    @Column(length = 20)
    private String cnpj;

    @Column(length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length = 255)
    private String receiptFooterMessage;

    public CompanySettings() {
    }

    public CompanySettings(Long id, String tradeName, String legalName, String cnpj, String phone,
                           String email, String address, String receiptFooterMessage) {
        this.id = id;
        this.tradeName = tradeName;
        this.legalName = legalName;
        this.cnpj = cnpj;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.receiptFooterMessage = receiptFooterMessage;
    }

    public Long getId() {
        return id;
    }

    public String getTradeName() {
        return tradeName;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getReceiptFooterMessage() {
        return receiptFooterMessage;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setReceiptFooterMessage(String receiptFooterMessage) {
        this.receiptFooterMessage = receiptFooterMessage;
    }
}