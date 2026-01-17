package com.br.pet_shop_management.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record AppointmentForm(@NotNull(message = "Owner ID is required.")
                              Long ownerId,

                              @NotNull(message = "Pet ID is required.")
                              Long petId,

                              @NotNull(message = "Scheduled date/time is required.")
                              LocalDateTime scheduledAt,

                              @Valid
                              @NotNull(message = "Items must be provided.")
                              List<AppointmentItemForm> items) {
}