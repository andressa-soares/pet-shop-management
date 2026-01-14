package com.br.pet_shop_management.application.mapper;

import com.br.pet_shop_management.api.dto.response.ClientDTO;
import com.br.pet_shop_management.api.dto.request.ClientForm;
import com.br.pet_shop_management.domain.entity.ClientEntity;
import com.br.pet_shop_management.domain.enums.ClientStatus;
import com.br.pet_shop_management.util.CpfUtils;

public final class ClientMapper {
    private ClientMapper() {}

    public static ClientDTO toDTO(ClientEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ClientEntity must not be null");
        }

        return new ClientDTO(
                entity.getId(),
                entity.getName(),
                CpfUtils.format(entity.getCpf()),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getStatus());
    }

    public static ClientEntity toEntity(ClientForm form, String normalizedCpf, ClientStatus clientStatus) {
        return new ClientEntity(
                form.name(),
                normalizedCpf,
                form.phone(),
                form.email(),
                form.address(),
                clientStatus);
    }
}