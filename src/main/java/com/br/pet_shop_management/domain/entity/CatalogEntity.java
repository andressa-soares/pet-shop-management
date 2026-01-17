package com.br.pet_shop_management.domain.entity;

import com.br.pet_shop_management.domain.enums.PetSize;
import com.br.pet_shop_management.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "catalog")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal priceSmall;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal priceMedium;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal priceLarge;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public CatalogEntity(String name, String description, Integer durationMinutes, BigDecimal priceSmall, BigDecimal priceMedium, BigDecimal priceLarge) {
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.priceSmall = priceSmall;
        this.priceMedium = priceMedium;
        this.priceLarge = priceLarge;
        this.status = Status.ACTIVE;
    }

    public void deactivate() {
        this.status = Status.INACTIVE;
    }

    public void activate() {
        this.status = Status.ACTIVE;
    }

    public BigDecimal getPriceByPetSize(PetSize petSize) {
        return switch (petSize) {
            case SMALL -> priceSmall;
            case MEDIUM -> priceMedium;
            case LARGE -> priceLarge;
        };

    }
}
