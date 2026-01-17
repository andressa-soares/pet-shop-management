package com.br.pet_shop_management.application.mapper;

import com.br.pet_shop_management.api.dto.request.PetForm;
import com.br.pet_shop_management.api.dto.response.PetDTO;
import com.br.pet_shop_management.domain.entity.OwnerEntity;
import com.br.pet_shop_management.domain.entity.PetEntity;

public final class PetMapper {
    private PetMapper() {}

    public static PetDTO toDTO(PetEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("PetEntity must not be null");
        }

        Long ownerId = entity.getOwner() != null ? entity.getOwner().getId() : null;

        return new PetDTO(
                entity.getId(),
                ownerId,
                entity.getName(),
                entity.getSpecies(),
                entity.getBreed(),
                entity.getSize(),
                entity.getBirthDate(),
                entity.getNotes(),
                entity.getAllergies());
    }

    public static PetEntity toEntity(PetForm form, OwnerEntity owner) {
        return new PetEntity(
                owner,
                form.name(),
                form.species(),
                form.breed(),
                form.size(),
                form.birthDate(),
                form.notes(),
                form.allergies());
    }
}
