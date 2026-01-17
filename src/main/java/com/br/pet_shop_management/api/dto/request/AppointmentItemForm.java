package com.br.pet_shop_management.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AppointmentItemForm(@NotNull(message = "Catalog ID is required.")
                                  Long catalogId,

                                  @NotNull(message = "Quantity is required.")
                                  @Min(value = 1, message = "Quantity must be at least 1.")
                                  Integer quantity) {
}