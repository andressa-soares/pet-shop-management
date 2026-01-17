package com.br.pet_shop_management.api.dto.response;

import com.br.pet_shop_management.domain.enums.OwnerStatus;

public record OwnerDTO(Long id,
                       String name,
                       String cpf,
                       String phone,
                       String email,
                       String address,
                       OwnerStatus status) {

}
