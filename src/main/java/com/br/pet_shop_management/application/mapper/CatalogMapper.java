package com.br.pet_shop_management.application.mapper;

import com.br.pet_shop_management.api.dto.request.CatalogForm;
import com.br.pet_shop_management.api.dto.response.CatalogDTO;
import com.br.pet_shop_management.domain.entity.CatalogEntity;

public final class CatalogMapper {
    private CatalogMapper() {}

    public static CatalogDTO toDTO(CatalogEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("CatalogEntity must not be null.");
        }

        return new CatalogDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getDurationMinutes(),
                entity.getPriceSmall(),
                entity.getPriceMedium(),
                entity.getPriceLarge(),
                entity.getStatus()
        );
    }

    public static CatalogEntity toEntity(CatalogForm form) {
        return new CatalogEntity(
                form.name(),
                form.description(),
                form.durationMinutes(),
                form.priceSmall(),
                form.priceMedium(),
                form.priceLarge()
        );
    }
}