package com.paulo.smartpet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Data
@NoArgsConstructor
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
}
