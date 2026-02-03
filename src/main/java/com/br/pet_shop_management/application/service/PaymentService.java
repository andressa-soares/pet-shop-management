package com.br.pet_shop_management.application.service;

import com.br.pet_shop_management.api.dto.request.PaymentForm;
import com.br.pet_shop_management.api.dto.response.PaymentDTO;
import com.br.pet_shop_management.application.exception.DomainRuleException;
import com.br.pet_shop_management.application.exception.InvalidInputException;
import com.br.pet_shop_management.application.mapper.PaymentMapper;
import com.br.pet_shop_management.domain.entity.AppointmentEntity;
import com.br.pet_shop_management.domain.entity.PaymentEntity;
import com.br.pet_shop_management.domain.enums.AppointmentStatus;
import com.br.pet_shop_management.domain.enums.PaymentMethod;
import com.br.pet_shop_management.domain.enums.Status;
import com.br.pet_shop_management.infrastructure.config.PaymentProperties;
import com.br.pet_shop_management.infrastructure.persistence.AppointmentRepository;
import com.br.pet_shop_management.infrastructure.persistence.PaymentRepository;
import com.br.pet_shop_management.util.MoneyUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentProperties paymentProperties;

    @Transactional
    public PaymentDTO registerPayment(Long appointmentId, PaymentForm form) {
        log.info("registerPayment started: appointmentId={}", appointmentId);

        if (appointmentId == null) {
            log.warn("registerPayment invalid input: appointmentId is null");
            throw new InvalidInputException("Appointment ID must be provided.");
        }

        if (form == null) {
            log.warn("registerPayment invalid input: form is null. appointmentId={}", appointmentId);
            throw new InvalidInputException("Payment form must be provided.");
        }

        AppointmentEntity appointment = appointmentRepository.findDetailedByIdForUpdate(appointmentId)
                .orElseThrow(() -> {
                    log.warn("registerPayment failed: appointment not found. appointmentId={}", appointmentId);
                    return new EntityNotFoundException("Appointment not found.");
                });

        if (appointment.getOwner().getStatus() == Status.INACTIVE) {
            log.warn("registerPayment blocked: owner inactive. appointmentId={}, ownerId={}",
                    appointment.getId(), appointment.getOwner().getId());
            throw new DomainRuleException("Appointments from inactive owners cannot be paid.");
        }

        if (appointment.getStatus() != AppointmentStatus.WAITING_PAYMENT) {
            log.warn("registerPayment blocked: invalid appointment status. appointmentId={}, status={}",
                    appointment.getId(), appointment.getStatus());
            throw new DomainRuleException("Payment can only be registered when appointment is WAITING_PAYMENT.");
        }

        if (paymentRepository.existsByAppointmentId(appointmentId)) {
            log.warn("registerPayment blocked: payment already exists. appointmentId={}", appointmentId);
            throw new DomainRuleException("This appointment already has a registered payment.");
        }

        int installments = resolveInstallments(form.method(), form.installments());
        BigDecimal finalAmount = calculateFinalAmount(appointment.getTotalGross(), form.method(), installments);

        log.info("registerPayment calculated: appointmentId={}, method={}, installments={}, totalGross={}, finalAmount={}",
                appointment.getId(), form.method(), installments, appointment.getTotalGross(), finalAmount);

        PaymentEntity payment = PaymentEntity.createApproved(
                appointment,
                form.method(),
                installments,
                finalAmount,
                LocalDateTime.now()
        );

        PaymentEntity saved = paymentRepository.save(payment);

        try {
            appointment.complete();
        } catch (IllegalStateException e) {
            log.warn("registerPayment blocked by state rule: appointmentId={}, msg={}", appointment.getId(), e.getMessage());
            throw new DomainRuleException(e.getMessage());
        }

        appointmentRepository.save(appointment);

        log.info("registerPayment completed: paymentId={}, appointmentId={}, appointmentStatusAfter={}",
                saved.getId(), appointment.getId(), appointment.getStatus());

        return PaymentMapper.toDTO(saved);
    }

    private int resolveInstallments(PaymentMethod method, Integer installments) {
        if (method == null) throw new InvalidInputException("Payment method is required.");

        if (method == PaymentMethod.CARD) {
            if (installments == null) throw new InvalidInputException("Installments are required for CARD payments.");
            if (installments < 1 || installments > 6) throw new InvalidInputException("Installments must be between 1 and 6.");
            return installments;
        }

        if (installments != null && installments != 1) {
            throw new InvalidInputException("Installments must be omitted or 1 for PIX/CASH payments.");
        }

        return 1;
    }

    private BigDecimal calculateFinalAmount(BigDecimal totalGross, PaymentMethod method, int installments) {
        if (totalGross == null || totalGross.signum() <= 0) {
            throw new DomainRuleException("Invalid appointment totalGross.");
        }

        BigDecimal gross = MoneyUtils.scale(totalGross);

        if (method == PaymentMethod.PIX || method == PaymentMethod.CASH) {
            return MoneyUtils.scale(gross.multiply(new BigDecimal("0.95")));
        }

        if (installments <= 2) {
            return MoneyUtils.scale(gross);
        }

        BigDecimal rate = paymentProperties.getInterestPerExtraInstallment();
        if (rate == null || rate.signum() < 0) {
            throw new DomainRuleException("Invalid card interest rate configuration.");
        }

        BigDecimal multiplier = BigDecimal.ONE.add(rate.multiply(BigDecimal.valueOf(installments - 2L)));
        return MoneyUtils.scale(gross.multiply(multiplier));
    }
}
