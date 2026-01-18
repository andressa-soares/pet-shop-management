package com.br.pet_shop_management.api.dto.request;

import com.br.pet_shop_management.domain.enums.PaymentMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaymentForm(@NotNull(message = "Payment method is required.")
                          PaymentMethod method,

                          @Min(value = 1, message = "Installments must be at least 1.")
                          @Max(value = 6, message = "Installments must be at most 6.")
                          Integer installments) {
}