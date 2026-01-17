package com.br.pet_shop_management.application.mapper;

import com.br.pet_shop_management.api.dto.response.OwnerDTO;
import com.br.pet_shop_management.api.dto.request.OwnerForm;
import com.br.pet_shop_management.domain.entity.OwnerEntity;
import com.br.pet_shop_management.domain.enums.OwnerStatus;
import com.br.pet_shop_management.util.CpfUtils;

public final class OwnerMapper {
    private OwnerMapper() {}

    public static OwnerDTO toDTO(OwnerEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("OwnerEntity must not be null");
        }

        return new OwnerDTO(
                entity.getId(),
                entity.getName(),
                CpfUtils.format(entity.getCpf()),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getStatus());
    }

    public static OwnerEntity toEntity(OwnerForm form, String normalizedCpf, OwnerStatus ownerStatus) {
        return new OwnerEntity(
                form.name(),
                normalizedCpf,
                form.phone(),
                form.email(),
                form.address(),
                ownerStatus);
    }
}