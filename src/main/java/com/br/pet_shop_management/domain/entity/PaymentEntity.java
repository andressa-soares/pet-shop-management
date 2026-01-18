package com.br.pet_shop_management.domain.entity;

import com.br.pet_shop_management.domain.enums.PaymentMethod;
import com.br.pet_shop_management.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private AppointmentEntity appointment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Integer installments;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public PaymentEntity(AppointmentEntity appointment, PaymentMethod method, PaymentStatus status, Integer installments, BigDecimal finalAmount, LocalDateTime createdAt) {
        this.appointment = appointment;
        this.method = method;
        this.status = status;
        this.installments = installments;
        this.finalAmount = finalAmount;
        this.createdAt = createdAt;
    }
}