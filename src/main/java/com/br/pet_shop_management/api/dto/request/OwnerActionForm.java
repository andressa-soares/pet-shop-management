package com.br.pet_shop_management.api.dto.request;

import com.br.pet_shop_management.api.dto.request.enums.OwnerAction;
import jakarta.validation.constraints.NotNull;

public record OwnerActionForm(@NotNull(message = "Action is required.")
                              OwnerAction action) {
}
