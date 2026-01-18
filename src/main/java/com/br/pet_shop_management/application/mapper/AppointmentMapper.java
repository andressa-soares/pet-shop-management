package com.br.pet_shop_management.application.mapper;

import com.br.pet_shop_management.api.dto.response.AppointmentDTO;
import com.br.pet_shop_management.api.dto.response.AppointmentItemDTO;
import com.br.pet_shop_management.domain.entity.AppointmentEntity;
import com.br.pet_shop_management.domain.entity.AppointmentItemEntity;

import java.util.List;

public final class AppointmentMapper {
    private AppointmentMapper() {}

    public static AppointmentDTO toDTO(AppointmentEntity appointment, List<AppointmentItemEntity> items) {
        if (appointment == null) {
            throw new IllegalArgumentException("AppointmentEntity must not be null.");
        }

        List<AppointmentItemDTO> itemDTOs = items == null ? List.of() : items.stream()
                .map(AppointmentMapper::toItemDTO)
                .toList();

        return new AppointmentDTO(
                appointment.getId(),
                appointment.getOwner().getId(),
                appointment.getPet().getId(),
                appointment.getScheduledAt(),
                appointment.getStatus(),
                appointment.getTotalGross(),
                appointment.getClosedAt(),
                itemDTOs);
    }

    public static AppointmentItemDTO toItemDTO(AppointmentItemEntity item) {
        if (item == null) {
            throw new IllegalArgumentException("AppointmentItemEntity must not be null.");
        }

        return new AppointmentItemDTO(
                item.getId(),
                item.getCatalog().getId(),
                item.getCatalog().getName(),
                item.getQuantity(),
                item.getUnitPriceApplied(),
                item.getSubtotal());
    }
}