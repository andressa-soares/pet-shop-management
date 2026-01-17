package com.br.pet_shop_management.api.dto.response;

import com.br.pet_shop_management.domain.enums.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AppointmentDTO(Long id,
                             Long ownerId,
                             Long petId,
                             LocalDateTime scheduledAt,
                             AppointmentStatus status,
                             BigDecimal totalGross,
                             LocalDateTime closedAt,
                             List<AppointmentItemDTO> items) {
}