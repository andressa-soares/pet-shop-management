package com.br.pet_shop_management.infrastructure.persistence;

import com.br.pet_shop_management.domain.entity.PaymentEntity;
import com.br.pet_shop_management.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    boolean existsByAppointmentId(Long appointmentId);
}