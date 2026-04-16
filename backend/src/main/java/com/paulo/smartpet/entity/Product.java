package com.paulo.smartpet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(unique = true, length = 100)
    private String barcode;

    private Boolean active = true;
}