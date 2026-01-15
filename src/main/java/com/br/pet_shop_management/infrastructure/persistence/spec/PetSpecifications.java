package com.br.pet_shop_management.infrastructure.persistence.spec;

import com.br.pet_shop_management.domain.entity.PetEntity;
import com.br.pet_shop_management.domain.enums.Breed;
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

    public static Specification<PetEntity> hasClientId(Long clientId) {
        return (root, query, cb) ->
                clientId == null ? cb.conjunction() : cb.equal(root.get("client").get("id"), clientId);
    }
}
