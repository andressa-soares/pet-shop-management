package com.br.pet_shop_management.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClientForm(@NotBlank(message = "Name is required.")
                         String name,

                         @NotBlank(message = "CPF is required.")
                         @Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$",message = "CPF must have 11 digits (with or without mask).")
                         String cpf,

                         @NotBlank(message = "Phone is required.")
                         @Pattern(regexp = "^\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}$", message = "Phone must match (DD) 99999-9999.")
                         String phone,

                         @Pattern(regexp = "^(?!\\s*$).+", message = "Email must not be blank.")
                         @Email(message = "Email must be valid.")
                         String email,

                         String address) {
}
