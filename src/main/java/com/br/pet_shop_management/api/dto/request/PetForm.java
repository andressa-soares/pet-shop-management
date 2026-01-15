package com.br.pet_shop_management.api.dto.request;

import com.br.pet_shop_management.domain.enums.Breed;
import com.br.pet_shop_management.domain.enums.PetSize;
import com.br.pet_shop_management.domain.enums.Species;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PetForm(@NotNull(message = "Client ID is required.")
                      Long clientId,

                      @NotBlank(message = "Name is required.")
                      @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters.")
                      String name,

                      @NotNull(message = "Species is required.")
                      Species species,

                      @NotNull(message = "Breed is required.")
                      Breed breed,

                      @NotNull(message = "Size is required.")
                      PetSize size,

                      @PastOrPresent(message = "Birth date cannot be in the future.")
                      LocalDate birthDate,

                      @Size(max = 500, message = "Notes must be at most 500 characters.")
                      String notes,

                      @Size(max = 500, message = "Notes must be at most 500 characters.")
                      String allergies) {
}