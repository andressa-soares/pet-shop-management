package com.br.pet_shop_management.api.dto.request;

import com.br.pet_shop_management.api.dto.request.enums.AppointmentAction;
import com.br.pet_shop_management.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentActionForm(@NotNull(message = "Action is required.")
                                    AppointmentAction action) {
}
