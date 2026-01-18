package com.br.pet_shop_management.application.mapper;

import com.br.pet_shop_management.api.dto.response.PaymentDTO;
import com.br.pet_shop_management.domain.entity.PaymentEntity;

public final class PaymentMapper {
    private PaymentMapper() {}

    public static PaymentDTO toDTO(PaymentEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("PaymentEntity must not be null.");
        }

        return new PaymentDTO(
                entity.getId(),
                entity.getAppointment().getId(),
                entity.getMethod(),
                entity.getStatus(),
                entity.getInstallments(),
                entity.getFinalAmount(),
                entity.getCreatedAt());
    }
}