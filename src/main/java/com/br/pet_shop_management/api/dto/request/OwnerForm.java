package com.br.pet_shop_management.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OwnerForm(@NotBlank(message = "Name is required.")
                        @Size(min = 2, max = 80, message = "Name must be between 2 and 80 characters.")
                        String name,

                        @NotBlank(message = "CPF is required.")
                        @Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$",message = "CPF must have 11 digits (with or without mask).")
                        String cpf,

                        @NotBlank(message = "Phone is required.")
                        @Pattern(regexp = "^\\d{10,11}$", message = "Phone must have 10 or 11 digits (no mask).")
                        String phone,

                        @Pattern(regexp = "^(?!\\s*$).+", message = "Email must not be blank.")
                        @Email(message = "Email must be valid.")
                        String email,

                        String address) {
}
