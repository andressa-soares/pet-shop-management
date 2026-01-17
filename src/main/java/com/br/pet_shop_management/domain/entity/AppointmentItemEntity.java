package com.br.pet_shop_management.domain.entity;

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

    public AppointmentItemEntity(AppointmentEntity appointment, CatalogEntity catalog, Integer quantity, BigDecimal unitPriceApplied, BigDecimal subtotal) {
        this.appointment = appointment;
        this.catalog = catalog;
        this.quantity = quantity;
        this.unitPriceApplied = unitPriceApplied;
        this.subtotal = subtotal;
    }
}

