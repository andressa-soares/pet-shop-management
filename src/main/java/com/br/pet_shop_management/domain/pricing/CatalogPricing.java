package com.br.pet_shop_management.domain.pricing;

import com.br.pet_shop_management.domain.entity.CatalogEntity;
import com.br.pet_shop_management.domain.enums.PetSize;

import java.math.BigDecimal;

import static com.br.pet_shop_management.domain.enums.PetSize.*;

public final class CatalogPricing {
    private CatalogPricing() {}

    public static BigDecimal resolveUnitPrice(CatalogEntity catalog, PetSize petSize) {
        if (catalog == null) {
            throw new IllegalArgumentException("CatalogEntity must not be null");
        }
        if (petSize == null) {
            throw new IllegalArgumentException("PetSize must not be null");
        }

        BigDecimal unitPrice = catalog.getPriceByPetSize(petSize);

        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalStateException("Resolved unit price must be greater than zero");
        }

        return unitPrice;
    }
}