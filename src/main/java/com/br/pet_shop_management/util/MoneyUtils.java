package com.br.pet_shop_management.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {
    private MoneyUtils() {}

    public static BigDecimal scale(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}