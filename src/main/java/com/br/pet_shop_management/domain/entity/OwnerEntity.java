package com.br.pet_shop_management.domain.entity;

import com.br.pet_shop_management.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "owners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false)
    private String phone;

    private String email;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public OwnerEntity(String name, String cpf, String phone, String email, String address, Status status) {
        this.name = name;
        this.cpf = cpf;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.status = status;
    }

    public void deactivate() {
        this.status = Status.INACTIVE;
    }

    public void activate() {
        this.status = Status.ACTIVE;
    }

    public void updateContactInfo(String phone, String email, String address) {
        if (phone != null) this.phone = phone;
        if (email != null) this.email = email;
        if (address != null) this.address = address;
    }
}
