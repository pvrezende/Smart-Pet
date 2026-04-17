package com.paulo.smartpet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_store_barcode", columnNames = {"store_id", "barcode"})
        }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String animalType;
    private String brand;
    private Double weight;
    private Double costPrice;
    private Double salePrice;
    private Integer stock;
    private Integer minimumStock;

    @Column(length = 100)
    private String barcode;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    private Boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Product() {
    }

    public Product(Long id, String name, String animalType, String brand, Double weight, Double costPrice,
                   Double salePrice, Integer stock, Integer minimumStock, String barcode, Store store, Boolean active) {
        this.id = id;
        this.name = name;
        this.animalType = animalType;
        this.brand = brand;
        this.weight = weight;
        this.costPrice = costPrice;
        this.salePrice = salePrice;
        this.stock = stock;
        this.minimumStock = minimumStock;
        this.barcode = barcode;
        this.store = store;
        this.active = active;
    }

    public Product(Long id, String name, String animalType, String brand, Double weight, Double costPrice,
                   Double salePrice, Integer stock, Integer minimumStock, String barcode, Store store,
                   Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.animalType = animalType;
        this.brand = brand;
        this.weight = weight;
        this.costPrice = costPrice;
        this.salePrice = salePrice;
        this.stock = stock;
        this.minimumStock = minimumStock;
        this.barcode = barcode;
        this.store = store;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAnimalType() {
        return animalType;
    }

    public String getBrand() {
        return brand;
    }

    public Double getWeight() {
        return weight;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public Integer getStock() {
        return stock;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public String getBarcode() {
        return barcode;
    }

    public Store getStore() {
        return store;
    }

    public Boolean getActive() {
        return active;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setAnimalType(String animalType) {
        this.animalType = animalType;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}