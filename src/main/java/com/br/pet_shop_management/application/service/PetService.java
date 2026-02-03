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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    public Page<PetDTO> findPets(Species species, Breed breed, Long ownerId, Pageable pageable) {
        log.info("findPets started: species={}, breed={}, ownerId={}, page={}", species, breed, ownerId, pageable);

        Specification<PetEntity> spec = Specification
                .where(PetSpecifications.hasSpecies(species))
                .and(PetSpecifications.hasBreed(breed))
                .and(PetSpecifications.hasOwnerId(ownerId))
                .and(PetSpecifications.hasOwnerStatus(Status.ACTIVE));

        Page<PetDTO> result = petRepository.findAll(spec, pageable).map(PetMapper::toDTO);

        log.info("findPets completed: resultCount={}", result.getNumberOfElements());
        return result;
    }

    public PetDTO findById(Long id) {
        log.info("findPetById: petId={}", id);

        PetEntity pet = petRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("findPetById failed: pet not found. petId={}", id);
                    return new EntityNotFoundException("Pet not found.");
                });

        log.info("findPetById completed: petId={}, ownerId={}", pet.getId(), pet.getOwner().getId());
        return PetMapper.toDTO(pet);
    }

    public PetDTO savePet(PetForm form) {
        log.info("savePet started: ownerId={}, name={}, species={}, breed={}, size={}",
                (form == null ? null : form.ownerId()),
                (form == null ? null : form.name()),
                (form == null ? null : form.species()),
                (form == null ? null : form.breed()),
                (form == null ? null : form.size()));

        if (form == null) {
            log.warn("savePet invalid input: form is null");
            throw new InvalidInputException("Pet form must be provided.");
        }

        OwnerEntity owner = ownerRepository.findById(form.ownerId())
                .orElseThrow(() -> {
                    log.warn("savePet failed: owner not found. ownerId={}", form.ownerId());
                    return new EntityNotFoundException("Owner not found.");
                });

        if (owner.getStatus() == Status.INACTIVE) {
            log.warn("savePet blocked: inactive owner. ownerId={}", owner.getId());
            throw new DomainRuleException("Inactive owners cannot have pets.");
        }

        String normalizedName = (form.name() == null) ? "" : form.name().trim();
        if (normalizedName.isBlank()) {
            log.warn("savePet invalid input: blank name. ownerId={}", owner.getId());
            throw new InvalidInputException("Name must be provided.");
        }

        if (petRepository.existsPetDuplicate(form.ownerId(), normalizedName)) {
            log.warn("savePet blocked: duplicate pet name for owner. ownerId={}, name={}", owner.getId(), normalizedName);
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

        PetEntity saved = petRepository.save(pet);

        log.info("savePet completed: petId={}, ownerId={}", saved.getId(), owner.getId());
        return PetMapper.toDTO(saved);
    }

    @Transactional
    public PetDTO patchPet(Long petId, PetUpdateForm form) {
        log.info("patchPet started: petId={}", petId);

        if (form == null) {
            log.warn("patchPet invalid input: form is null. petId={}", petId);
            throw new InvalidInputException("Patch form must be provided.");
        }

        PetEntity pet = findPet(petId);

        boolean hasAnyField = form.notes() != null || form.allergies() != null;
        if (!hasAnyField) {
            log.warn("patchPet invalid input: no fields provided. petId={}", petId);
            throw new InvalidInputException("At least one field must be provided: notes or allergies.");
        }

        if (form.notes() != null) {
            if (form.notes().isBlank()) {
                log.warn("patchPet invalid input: blank notes. petId={}", petId);
                throw new InvalidInputException("Notes must not be blank.");
            }
            pet.updateNotes(form.notes());
        }

        if (form.allergies() != null) {
            if (form.allergies().isBlank()) {
                log.warn("patchPet invalid input: blank allergies. petId={}", petId);
                throw new InvalidInputException("Allergies must not be blank.");
            }
            pet.updateAllergies(form.allergies());
        }

        PetEntity saved = petRepository.save(pet);

        log.info("patchPet completed: petId={}", saved.getId());
        return PetMapper.toDTO(saved);
    }

    @Transactional
    public void deletePet(Long id) {
        log.info("deletePet started: petId={}", id);

        PetEntity pet = findPet(id);
        petRepository.delete(pet);

        log.info("deletePet completed: petId={}", pet.getId());
    }

    private PetEntity findPet(Long id) {
        if (id == null) {
            log.warn("findPet invalid input: id is null");
            throw new InvalidInputException("Pet ID must be provided.");
        }

        PetEntity pet = petRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("findPet failed: pet not found. petId={}", id);
                    return new EntityNotFoundException("Pet not found.");
                });

        if (pet.getOwner().getStatus() == Status.INACTIVE) {
            log.warn("findPet blocked: inactive owner. petId={}, ownerId={}", pet.getId(), pet.getOwner().getId());
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
