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

    public boolean isLocked() {
        return this.status == AppointmentStatus.WAITING_PAYMENT || this.status == AppointmentStatus.COMPLETED;
    }

    public boolean isCanceled() {
        return this.status == AppointmentStatus.CANCELED;
    }

    public void updateTotalGross(BigDecimal totalGross) {
        this.totalGross = totalGross;
    }

    public void cancel() {
        if (this.status == AppointmentStatus.WAITING_PAYMENT) {
            throw new IllegalStateException("Appointments waiting for payment cannot be canceled.");
        }
        if (this.status == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Completed appointments cannot be canceled.");
        }
        if (this.status == AppointmentStatus.CANCELED) {
            throw new IllegalStateException("Appointment is already canceled.");
        }

        this.status = AppointmentStatus.CANCELED;
    }

    public void closeForPayment(LocalDateTime now) {
        if (this.status == AppointmentStatus.CANCELED) {
            throw new IllegalStateException("Canceled appointments cannot be closed for payment.");
        }
        this.status = AppointmentStatus.WAITING_PAYMENT;
        this.closedAt = now;
    }
}