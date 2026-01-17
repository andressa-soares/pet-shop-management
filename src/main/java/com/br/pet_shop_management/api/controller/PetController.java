package com.br.pet_shop_management.api.controller;

import com.br.pet_shop_management.api.dto.request.PetForm;
import com.br.pet_shop_management.api.dto.request.PetUpdateForm;
import com.br.pet_shop_management.api.dto.response.PetDTO;
import com.br.pet_shop_management.application.service.PetService;
import com.br.pet_shop_management.domain.enums.Breed;
import com.br.pet_shop_management.domain.enums.Species;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping
    public Page<PetDTO> findPets(
            @RequestParam(required = false) Species species,
            @RequestParam(required = false) Breed breed,
            @RequestParam(required = false) Long ownerId,
            Pageable pageable
    ) {
        return petService.findPets(species, breed, ownerId, pageable);
    }

    @GetMapping("/{id:\\d+}")
    public PetDTO findById(@PathVariable Long id) {
        return petService.findById(id);
    }

    @GetMapping("/breeds")
    public List<Breed> listBreedsBySpecies(@RequestParam Species species) {
        return Breed.bySpecies(species);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PetDTO savePet(@Valid @RequestBody PetForm petForm) {
        return petService.savePet(petForm);
    }

    @PatchMapping("/{id:\\d+}/notes")
    public PetDTO updateNotes(@PathVariable Long id,
                              @RequestBody PetUpdateForm form) {
        return petService.updateNotes(id, form.notes());
    }

    @DeleteMapping("/{id:\\d+}/notes")
    public PetDTO deleteNotes(@PathVariable Long id) {
        return petService.deleteNotes(id);
    }

    @PatchMapping("/{id:\\d+}/allergies")
    public PetDTO updateAllergies(@PathVariable Long id,
                                  @RequestBody PetUpdateForm form) {
        return petService.updateAllergies(id, form.allergies());
    }

    @DeleteMapping("/{id:\\d+}/allergies")
    public PetDTO deleteAllergies(@PathVariable Long id) {
        return petService.deleteAllergies(id);
    }

    @DeleteMapping("/{id:\\d+}")
    public PetDTO deletePet(@PathVariable Long id) {
        return petService.deletePet(id);
    }
}
