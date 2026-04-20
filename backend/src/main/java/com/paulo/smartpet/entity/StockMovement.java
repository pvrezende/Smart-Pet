package com.paulo.smartpet.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String movementType;
    private Integer quantity;
    private Integer previousStock;
    private Integer currentStock;
    private String observation;
    private LocalDateTime movementDate = LocalDateTime.now();

    public StockMovement() {
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getMovementType() {
        return movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getPreviousStock() {
        return previousStock;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public String getObservation() {
        return observation;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPreviousStock(Integer previousStock) {
        this.previousStock = previousStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
    }
}