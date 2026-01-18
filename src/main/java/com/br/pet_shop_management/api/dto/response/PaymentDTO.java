package com.br.pet_shop_management.api.dto.response;

import com.br.pet_shop_management.domain.enums.PaymentMethod;
import com.br.pet_shop_management.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDTO(Long id,
                         Long appointmentId,
                         PaymentMethod method,
                         PaymentStatus status,
                         Integer installments,
                         BigDecimal finalAmount,
                         LocalDateTime createdAt) {
}