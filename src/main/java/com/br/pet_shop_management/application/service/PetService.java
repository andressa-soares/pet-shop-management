package com.br.pet_shop_management.application.service;

import com.br.pet_shop_management.api.dto.request.PetForm;
import com.br.pet_shop_management.api.dto.response.PetDTO;
import com.br.pet_shop_management.application.exception.BusinessException;
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
                .and(PetSpecifications.hasOwnerStatus(Status.ACTIVE)); // filtro novo

        return petRepository.findAll(spec, pageable)
                .map(PetMapper::toDTO);
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
            throw new BusinessException("Inactive owners cannot have pets.");
        }

        if (petRepository.existsPetDuplicate(form.ownerId(), form.name().trim())) {
            throw new BusinessException("This owner already has a pet with the same name.");
        }

        validateBreedMatchesSpecies(form.species(), form.breed());

        PetEntity pet = PetMapper.toEntity(form, owner);
        return PetMapper.toDTO(petRepository.save(pet));
    }

    @Transactional
    public PetDTO updateNotes(Long petId, String notes) {
        PetEntity pet = findPet(petId);
        pet.updateNotes(notes);
        return PetMapper.toDTO(petRepository.save(pet));
    }

    @Transactional
    public PetDTO deleteNotes(Long petId) {
        PetEntity pet = findPet(petId);
        pet.clearNotes();
        return PetMapper.toDTO(petRepository.save(pet));
    }

    @Transactional
    public PetDTO updateAllergies(Long petId, String allergies) {
        PetEntity pet = findPet(petId);
        pet.updateAllergies(allergies);
        return PetMapper.toDTO(petRepository.save(pet));
    }

    @Transactional
    public PetDTO deleteAllergies(Long petId) {
        PetEntity pet = findPet(petId);
        pet.clearAllergies();
        return PetMapper.toDTO(petRepository.save(pet));
    }

    @Transactional
    public void deletePet(Long id) {
        PetEntity pet = findPet(id);
        petRepository.delete(pet);
        PetMapper.toDTO(pet);
    }

    private PetEntity findPet(Long id) {
        if (id == null) {
            throw new BusinessException("Pet ID must be provided.");
        }

        PetEntity pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pet not found."));

        if (pet.getOwner().getStatus() == Status.INACTIVE) {
            throw new BusinessException("Pets from inactive owners cannot be updated.");
        }

        return pet;
    }

    private void validateBreedMatchesSpecies(Species species, Breed breed) {
        if (species == null) {
            throw new BusinessException("Species must be provided.");
        }
        if (breed == null) {
            throw new BusinessException("Breed must be provided.");
        }

        if (!breed.belongsTo(species)) {
            throw new BusinessException("Breed is not valid for species " + species + ".");
        }
    }
}