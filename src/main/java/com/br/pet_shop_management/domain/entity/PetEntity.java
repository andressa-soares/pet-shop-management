package com.br.pet_shop_management.domain.entity;

import com.br.pet_shop_management.domain.enums.Breed;
import com.br.pet_shop_management.domain.enums.PetSize;
import com.br.pet_shop_management.domain.enums.Species;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "pets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private OwnerEntity owner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Species species;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Breed breed;

    @Column(nullable=false, updatable=false)
    @Enumerated(EnumType.STRING)
    private PetSize size;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String notes;

    private String allergies;

    public PetEntity(OwnerEntity owner, String name, Species species, Breed breed, PetSize size, LocalDate birthDate, String notes, String allergies) {
        this.owner = owner;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.size = size;
        this.birthDate = birthDate;
        this.notes = notes;
        this.allergies = allergies;
    }

    public void updateNotes(String notes) {
        this.notes = notes;
    }

    public void updateAllergies(String allergies) {
        this.allergies = allergies;
    }

    public void clearNotes() {
        this.notes = null;
    }

    public void clearAllergies() {
        this.allergies = null;
    }
}