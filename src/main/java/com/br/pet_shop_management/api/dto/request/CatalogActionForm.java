package com.br.pet_shop_management.api.dto.request;

import com.br.pet_shop_management.api.dto.request.enums.CatalogAction;
import jakarta.validation.constraints.NotNull;

public record CatalogActionForm(@NotNull(message = "Action is required.")
                                CatalogAction action) {
}
