package com.br.pet_shop_management.domain.enums;

import java.util.Arrays;
import java.util.List;

public enum Breed {
    // DOG
    LABRADOR_RETRIEVER(Species.DOG),
    GOLDEN_RETRIEVER(Species.DOG),
    GERMAN_SHEPHERD(Species.DOG),
    FRENCH_BULLDOG(Species.DOG),
    ENGLISH_BULLDOG(Species.DOG),
    POODLE(Species.DOG),
    BEAGLE(Species.DOG),
    ROTTWEILER(Species.DOG),
    YORKSHIRE_TERRIER(Species.DOG),
    DACHSHUND(Species.DOG),
    SHIH_TZU(Species.DOG),
    PUG(Species.DOG),
    BOXER(Species.DOG),
    BORDER_COLLIE(Species.DOG),
    SIBERIAN_HUSKY(Species.DOG),
    CHIHUAHUA(Species.DOG),
    PIT_BULL(Species.DOG),
    MIXED_DOG(Species.DOG),

    // CAT
    PERSIAN(Species.CAT),
    SIAMESE(Species.CAT),
    MAINE_COON(Species.CAT),
    RAGDOLL(Species.CAT),
    BENGAL(Species.CAT),
    BRITISH_SHORTHAIR(Species.CAT),
    SPHYNX(Species.CAT),
    SCOTTISH_FOLD(Species.CAT),
    ABYSSINIAN(Species.CAT),
    AMERICAN_SHORTHAIR(Species.CAT),
    MIXED_CAT(Species.CAT);

    private final Species species;

    Breed(Species species) {
        this.species = species;
    }

    public Species getSpecies() {
        return species;
    }

    public boolean belongsTo(Species species) {
        return this.species == species;
    }

    public static List<Breed> bySpecies(Species species) {
        return Arrays.stream(values())
                .filter(breed -> breed.species == species)
                .toList();
    }
}