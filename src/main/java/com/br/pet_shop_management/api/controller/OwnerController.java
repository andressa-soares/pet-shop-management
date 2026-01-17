package com.br.pet_shop_management.api.controller;

import com.br.pet_shop_management.api.dto.response.OwnerDTO;
import com.br.pet_shop_management.api.dto.request.OwnerForm;
import com.br.pet_shop_management.api.dto.request.OwnerUpdateForm;
import com.br.pet_shop_management.application.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping
    public List<OwnerDTO> findAll() {
        return ownerService.findAll();
    }

    @GetMapping("/{cpf}")
    public OwnerDTO findByCpf(@PathVariable String cpf) {
        return ownerService.findByCpf(cpf);
    }

    @GetMapping("/{id}")
    public OwnerDTO findById(@PathVariable Long id) {
        return ownerService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OwnerDTO saveOwner(@Valid @RequestBody OwnerForm ownerForm) {
        return ownerService.saveOwner(ownerForm);
    }

    @PatchMapping("/{cpf}")
    public OwnerDTO updateOwnerContact(@PathVariable String cpf, @Valid @RequestBody OwnerUpdateForm form) {
        return ownerService.updateOwnerContact(cpf, form);
    }

    @PatchMapping("/{cpf}/activate")
    public OwnerDTO activateOwner(@PathVariable String cpf) {
        return ownerService.activateOwner(cpf);
    }

    @DeleteMapping("/{cpf}")
    public OwnerDTO deleteOwner(@PathVariable String cpf) {
        return ownerService.deleteOwner(cpf);
    }
}
