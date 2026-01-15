package com.br.pet_shop_management.api.dto.response;

import com.br.pet_shop_management.domain.enums.Breed;
import com.br.pet_shop_management.domain.enums.PetSize;
import com.br.pet_shop_management.domain.enums.Species;

import java.time.LocalDate;

public record PetDTO(Long id,
                     Long clientId,
                     String name,
                     Species species,
                     Breed breed,
                     PetSize size,
                     LocalDate birthDate,
                     String notes,
                     String allergies) {
}