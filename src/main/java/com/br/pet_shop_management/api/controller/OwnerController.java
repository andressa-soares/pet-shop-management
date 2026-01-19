package com.br.pet_shop_management.api.controller;

import com.br.pet_shop_management.api.dto.request.OwnerActionForm;
import com.br.pet_shop_management.api.dto.response.OwnerDTO;
import com.br.pet_shop_management.api.dto.request.OwnerForm;
import com.br.pet_shop_management.api.dto.request.OwnerUpdateForm;
import com.br.pet_shop_management.application.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping
    public Page<OwnerDTO> findAll(Pageable pageable) {
        return ownerService.findAll(pageable);
    }

    @GetMapping("/{cpf}")
    public OwnerDTO findByCpf(@PathVariable String cpf) {
        return ownerService.findByCpf(cpf);
    }

    @GetMapping("/{id:\\d+}")
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

    @PostMapping("/{cpf}/actions")
    public OwnerDTO applyAction(@PathVariable String cpf, @Valid @RequestBody OwnerActionForm form) {
        return ownerService.applyAction(cpf, form.action());
    }
}
