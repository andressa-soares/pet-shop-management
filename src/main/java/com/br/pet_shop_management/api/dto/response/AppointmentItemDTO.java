package com.br.pet_shop_management.api.dto.response;

import java.math.BigDecimal;

public record AppointmentItemDTO(Long id,
                                 Long catalogId,
                                 String catalogName,
                                 Integer quantity,
                                 BigDecimal unitPriceApplied,
                                 BigDecimal subtotal) {
}
