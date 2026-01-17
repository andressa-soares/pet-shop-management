package com.br.pet_shop_management.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record OwnerUpdateForm(
        @Pattern(regexp = "^\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}$",message = "Phone must match (DD) 99999-9999.")
        String phone,

        @Pattern(regexp = "^(?!\\s*$).+",message = "Email must not be blank.")
        @Email(message = "Email must be valid.")
        String email,

        @Pattern(regexp = "^(?!\\s*$).+",message = "Address must not be blank.")
        String address) {
}