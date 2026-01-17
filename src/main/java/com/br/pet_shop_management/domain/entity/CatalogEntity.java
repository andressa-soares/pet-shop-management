package com.br.pet_shop_management.domain.entity;

import com.br.pet_shop_management.domain.enums.PetSize;
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

    @Column(nullable = false)
    private Boolean active;

    public CatalogEntity(String name, String description, Integer durationMinutes, BigDecimal priceSmall, BigDecimal priceMedium, BigDecimal priceLarge, Boolean active) {
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.priceSmall = priceSmall;
        this.priceMedium = priceMedium;
        this.priceLarge = priceLarge;
        this.active = active;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public BigDecimal getPriceByPetSize(PetSize petSize) {
        return switch (petSize) {
            case SMALL -> priceSmall;
            case MEDIUM -> priceMedium;
            case LARGE -> priceLarge;
        };

    }
}
