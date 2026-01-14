package com.br.pet_shop_management.api.exception;

import java.time.OffsetDateTime;

public record ApiError(int status,
                       String error,
                       String message,
                       String path,
                       OffsetDateTime timestamp) {
}