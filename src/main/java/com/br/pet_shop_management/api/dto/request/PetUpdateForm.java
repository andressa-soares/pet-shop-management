package com.br.pet_shop_management.api.dto.request;

import jakarta.validation.constraints.Size;

public record PetUpdateForm(@Size(max = 500, message = "Notes must be at most 500 characters.")
                            String notes,
                            @Size(max = 500, message = "Allergies must be at most 500 characters.")
                            String allergies) {
}
