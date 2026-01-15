package com.br.pet_shop_management.domain.entity;

import com.br.pet_shop_management.domain.enums.ClientStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientEntity {
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
    private ClientStatus status;

    public ClientEntity(String name, String cpf, String phone, String email, String address, ClientStatus status) {
        this.name = name;
        this.cpf = cpf;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.status = status;
    }

    public void inactivate() {
        this.status = ClientStatus.INACTIVE;
    }

    public void activate() {
        this.status = ClientStatus.ACTIVE;
    }

    public void updateContactInfo(String phone, String email, String address) {
        if (phone != null) this.phone = phone;
        if (email != null) this.email = email;
        if (address != null) this.address = address;
    }
}
