package com.br.pet_shop_management.api.dto.response;

import com.br.pet_shop_management.domain.enums.ClientStatus;

public record ClientDTO(Long id,
                        String name,
                        String cpf,
                        String phone,
                        String email,
                        String address,
                        ClientStatus status) {

}
