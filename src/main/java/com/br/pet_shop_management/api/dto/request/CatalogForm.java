package com.br.pet_shop_management.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CatalogForm(@NotBlank(message = "Name is required.")
                          @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters.")
                          String name,

                          @Size(max = 255, message = "Description must be at most 255 characters.")
                          String description,

                          @NotNull(message = "Duration is required.")
                          @DecimalMin(value = "1", message = "Duration must be at least 1 minute.")
                          Integer durationMinutes,

                          @NotNull(message = "Price for small pets is required.")
                          @DecimalMin(value = "0.01", message = "Price for small pets must be greater than zero.")
                          BigDecimal priceSmall,

                          @NotNull(message = "Price for medium pets is required.")
                          @DecimalMin(value = "0.01", message = "Price for medium pets must be greater than zero.")
                          BigDecimal priceMedium,

                          @NotNull(message = "Price for large pets is required.")
                          @DecimalMin(value = "0.01", message = "Price for large pets must be greater than zero.")
                          BigDecimal priceLarge) {
}