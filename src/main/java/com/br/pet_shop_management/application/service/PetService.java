package com.br.pet_shop_management.application.service;

import com.br.pet_shop_management.api.dto.request.PetForm;
import com.br.pet_shop_management.api.dto.request.PetUpdateForm;
import com.br.pet_shop_management.api.dto.response.PetDTO;
import com.br.pet_shop_management.application.exception.DomainRuleException;
import com.br.pet_shop_management.application.exception.InvalidInputException;
import com.br.pet_shop_management.application.mapper.PetMapper;
import com.br.pet_shop_management.domain.entity.OwnerEntity;
import com.br.pet_shop_management.domain.entity.PetEntity;
import com.br.pet_shop_management.domain.enums.Breed;
import com.br.pet_shop_management.domain.enums.Status;
import com.br.pet_shop_management.domain.enums.Species;
import com.br.pet_shop_management.infrastructure.persistence.OwnerRepository;
import com.br.pet_shop_management.infrastructure.persistence.PetRepository;
import com.br.pet_shop_management.infrastructure.persistence.spec.PetSpecifications;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    public Page<PetDTO> findPets(Species species, Breed breed, Long ownerId, Pageable pageable) {
        Specification<PetEntity> spec = Specification
                .where(PetSpecifications.hasSpecies(species))
                .and(PetSpecifications.hasBreed(breed))
                .and(PetSpecifications.hasOwnerId(ownerId))
                .and(PetSpecifications.hasOwnerStatus(Status.ACTIVE));

        return petRepository.findAll(spec, pageable).map(PetMapper::toDTO);
    }

    public PetDTO findById(Long id) {
        PetEntity pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pet not found."));
        return PetMapper.toDTO(pet);
    }

    public PetDTO savePet(PetForm form) {
        OwnerEntity owner = ownerRepository.findById(form.ownerId())
                .orElseThrow(() -> new EntityNotFoundException("Owner not found."));

        if (owner.getStatus() == Status.INACTIVE) {
            throw new DomainRuleException("Inactive owners cannot have pets.");
        }

        String normalizedName = form.name().trim();
        if (normalizedName.isBlank()) {
            throw new InvalidInputException("Name must be provided.");
        }

        if (petRepository.existsPetDuplicate(form.ownerId(), normalizedName)) {
            throw new DomainRuleException("This owner already has a pet with the same name.");
        }

        validateBreedMatchesSpecies(form.species(), form.breed());

        PetEntity pet = PetMapper.toEntity(new PetForm(
                form.ownerId(),
                normalizedName,
                form.species(),
                form.breed(),
                form.size(),
                form.birthDate(),
                form.notes(),
                form.allergies()
        ), owner);

        return PetMapper.toDTO(petRepository.save(pet));
    }

    @Transactional
    public PetDTO patchPet(Long petId, PetUpdateForm form) {
        PetEntity pet = findPet(petId);

        boolean hasAnyField = form.notes() != null || form.allergies() != null;
        if (!hasAnyField) {
            throw new InvalidInputException("At least one field must be provided: notes or allergies.");
        }

        if (form.notes() != null) {
            if (form.notes().isBlank()) throw new InvalidInputException("Notes must not be blank.");
            pet.updateNotes(form.notes());
        }

        if (form.allergies() != null) {
            if (form.allergies().isBlank()) throw new InvalidInputException("Allergies must not be blank.");
            pet.updateAllergies(form.allergies());
        }

        return PetMapper.toDTO(petRepository.save(pet));
    }

    @Transactional
    public void deletePet(Long id) {
        PetEntity pet = findPet(id);
        petRepository.delete(pet);
    }

    private PetEntity findPet(Long id) {
        if (id == null) throw new InvalidInputException("Pet ID must be provided.");

        PetEntity pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pet not found."));

        if (pet.getOwner().getStatus() == Status.INACTIVE) {
            throw new DomainRuleException("Pets from inactive owners cannot be updated.");
        }

        return pet;
    }

    private void validateBreedMatchesSpecies(Species species, Breed breed) {
        if (species == null) throw new InvalidInputException("Species must be provided.");
        if (breed == null) throw new InvalidInputException("Breed must be provided.");

        if (!breed.belongsTo(species)) {
            throw new InvalidInputException("Breed is not valid for species " + species + ".");
        }
    }
}