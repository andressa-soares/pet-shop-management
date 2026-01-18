package com.br.pet_shop_management.domain.entity;

import com.br.pet_shop_management.util.MoneyUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "appointment_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppointmentItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private AppointmentEntity appointment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_id", nullable = false)
    private CatalogEntity catalog;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceApplied;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    private AppointmentItemEntity(AppointmentEntity appointment, CatalogEntity catalog, Integer quantity, BigDecimal unitPriceApplied) {
        if (appointment == null) throw new IllegalArgumentException("Appointment must be provided.");
        if (catalog == null) throw new IllegalArgumentException("Catalog must be provided.");
        if (quantity == null) throw new IllegalArgumentException("Quantity must be provided.");
        if (unitPriceApplied == null) throw new IllegalArgumentException("unitPriceApplied must be provided.");

        if (appointment.isLocked()) {
            throw new IllegalStateException("Cannot add items when appointment is locked.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be >= 1.");
        }

        BigDecimal price = MoneyUtils.scale(unitPriceApplied);
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("unitPriceApplied must be > 0.");
        }

        this.appointment = appointment;
        this.catalog = catalog;
        this.quantity = quantity;
        this.unitPriceApplied = price;

        BigDecimal rawSubtotal = price.multiply(BigDecimal.valueOf(quantity.longValue()));
        this.subtotal = MoneyUtils.scale(rawSubtotal);
    }

    public static AppointmentItemEntity create(AppointmentEntity appointment, CatalogEntity catalog, Integer quantity, BigDecimal unitPriceApplied) {
        return new AppointmentItemEntity(appointment, catalog, quantity, unitPriceApplied);
    }
}

