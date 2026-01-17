package com.br.pet_shop_management.infrastructure.persistence.spec;

import com.br.pet_shop_management.domain.entity.PetEntity;
import com.br.pet_shop_management.domain.enums.Breed;
import com.br.pet_shop_management.domain.enums.Status;
import com.br.pet_shop_management.domain.enums.Species;
import org.springframework.data.jpa.domain.Specification;

public final class PetSpecifications {
    private PetSpecifications() {}

    public static Specification<PetEntity> hasSpecies(Species species) {
        return (root, query, cb) ->
                species == null ? cb.conjunction() : cb.equal(root.get("species"), species);
    }

    public static Specification<PetEntity> hasBreed(Breed breed) {
        return (root, query, cb) ->
                breed == null ? cb.conjunction() : cb.equal(root.get("breed"), breed);
    }

    public static Specification<PetEntity> hasOwnerId(Long ownerId) {
        return (root, query, cb) ->
                ownerId == null ? cb.conjunction() : cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<PetEntity> hasOwnerStatus(Status status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("owner").get("status"), status);
    }
}
