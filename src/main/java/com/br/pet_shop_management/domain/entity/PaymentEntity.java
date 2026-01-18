package com.br.pet_shop_management.domain.entity;

import com.br.pet_shop_management.domain.enums.PaymentMethod;
import com.br.pet_shop_management.domain.enums.PaymentStatus;
import com.br.pet_shop_management.util.MoneyUtils;
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

    @Version
    private Long version;

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

    private PaymentEntity(AppointmentEntity appointment, PaymentMethod method, PaymentStatus status, Integer installments, BigDecimal finalAmount, LocalDateTime createdAt) {
        this.appointment = appointment;
        this.method = method;
        this.status = status;
        this.installments = installments;
        this.finalAmount = finalAmount;
        this.createdAt = createdAt;

    if (appointment == null) throw new IllegalArgumentException("Appointment must be provided.");
    if (method == null) throw new IllegalArgumentException("Payment method must be provided.");
    if (status == null) throw new IllegalArgumentException("Payment status must be provided.");
    if (installments == null) throw new IllegalArgumentException("Installments must be provided.");
    if (createdAt == null) throw new IllegalArgumentException("createdAt must be provided.");
    if (finalAmount == null) throw new IllegalArgumentException("finalAmount must be provided.");

    if (installments < 1 || installments > 6) {
        throw new IllegalArgumentException("Installments must be between 1 and 6.");
    }

    if (method != PaymentMethod.CARD && installments != 1) {
        throw new IllegalArgumentException("Installments must be 1 for PIX/CASH payments.");
    }

    BigDecimal scaled = MoneyUtils.scale(finalAmount);
    if (scaled.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("finalAmount must be > 0.");
    }

    this.finalAmount = scaled;
    }

    public static PaymentEntity createApproved(AppointmentEntity appointment, PaymentMethod method, int installments, BigDecimal finalAmount, LocalDateTime createdAt) {
        return new PaymentEntity(appointment, method, PaymentStatus.APPROVED, installments, finalAmount, createdAt);
    }

    public static PaymentEntity createPending(AppointmentEntity appointment, PaymentMethod method, int installments, BigDecimal finalAmount, LocalDateTime createdAt) {
        return new PaymentEntity(appointment, method, PaymentStatus.PENDING, installments, finalAmount, createdAt);
    }

    public void approve() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payments can be approved.");
        }
        this.status = PaymentStatus.APPROVED;
    }

    public void reject() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payments can be rejected.");
        }
        this.status = PaymentStatus.REJECTED;
    }
}