package com.br.pet_shop_management.api.dto.response;

import com.br.pet_shop_management.domain.enums.Status;

import java.math.BigDecimal;

public record CatalogDTO(Long id,
                         String name,
                         String description,
                         Integer durationMinutes,
                         BigDecimal priceSmall,
                         BigDecimal priceMedium,
                         BigDecimal priceLarge,
                         Status status) {
}