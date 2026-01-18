package com.br.pet_shop_management.domain.entity;

import com.br.pet_shop_management.domain.enums.AppointmentStatus;
import com.br.pet_shop_management.util.MoneyUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppointmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private OwnerEntity owner;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private PetEntity pet;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalGross;

    private LocalDateTime closedAt;

    public AppointmentEntity(OwnerEntity owner, PetEntity pet, LocalDateTime scheduledAt) {
        this.owner = owner;
        this.pet = pet;
        this.scheduledAt = scheduledAt;
        this.status = AppointmentStatus.SCHEDULED;
        this.totalGross = MoneyUtils.zero();
    }

    public void start() {
        if (this.status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Only SCHEDULED appointments can start.");
        }
        this.status = AppointmentStatus.IN_PROGRESS;
    }

    public boolean isLocked() {
        return this.status == AppointmentStatus.WAITING_PAYMENT || this.status == AppointmentStatus.COMPLETED;
    }

    public boolean isCanceled() {
        return this.status == AppointmentStatus.CANCELED;
    }

    public void updateTotalGross(BigDecimal totalGross) {
        if (isLocked()) {
            throw new IllegalStateException("Cannot change totalGross when appointment is locked.");
        }
        if (totalGross == null) {
            throw new IllegalArgumentException("totalGross must be provided.");
        }
        if (totalGross.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("totalGross cannot be negative.");
        }
        this.totalGross = MoneyUtils.scale(totalGross);
    }

    public void cancel() {
        if (this.status != AppointmentStatus.SCHEDULED && this.status != AppointmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Appointment can only be canceled when SCHEDULED or IN_PROGRESS.");
        }
        this.status = AppointmentStatus.CANCELED;
    }

    public void closeForPayment(LocalDateTime now) {
        if (now == null) throw new IllegalArgumentException("now must be provided.");

        if (this.status != AppointmentStatus.SCHEDULED && this.status != AppointmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only SCHEDULED or IN_PROGRESS appointments can be closed for payment.");
        }
        if (this.closedAt != null) {
            throw new IllegalStateException("Appointment is already closed for payment.");
        }
        if (this.totalGross == null || this.totalGross.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Appointment must have items (totalGross > 0) to close for payment.");
        }

        this.status = AppointmentStatus.WAITING_PAYMENT;
        this.closedAt = now;
    }

    public void complete() {
        if (this.status != AppointmentStatus.WAITING_PAYMENT) {
            throw new IllegalStateException("Appointment must be waiting for payment to be completed.");
        }

        this.status = AppointmentStatus.COMPLETED;
    }
}