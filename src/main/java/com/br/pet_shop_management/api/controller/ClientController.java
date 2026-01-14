package com.br.pet_shop_management.api.controller;

import com.br.pet_shop_management.api.dto.response.ClientDTO;
import com.br.pet_shop_management.api.dto.request.ClientForm;
import com.br.pet_shop_management.api.dto.request.ClientUpdateForm;
import com.br.pet_shop_management.application.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public List<ClientDTO> findAll() {
        return clientService.findAll();
    }

    @GetMapping("/{cpf}")
    public ClientDTO findByCpf(@PathVariable String cpf) {
        return clientService.findByCpf(cpf);
    }

    @GetMapping("/{id}")
    public ClientDTO findById(@PathVariable Long id) {
        return clientService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDTO saveClient(@Valid @RequestBody ClientForm clientForm) {
        return clientService.saveClient(clientForm);
    }

    @PatchMapping("/{cpf}")
    public ClientDTO updateClientContact(@PathVariable String cpf, @Valid @RequestBody ClientUpdateForm form) {
        return clientService.updateClientContact(cpf, form);
    }

    @PatchMapping("/{cpf}/activate")
    public ClientDTO activateClient(@PathVariable String cpf) {
        return clientService.activateClient(cpf);
    }

    @DeleteMapping("/{cpf}")
    public ClientDTO deleteClient(@PathVariable String cpf) {
        return clientService.deleteClient(cpf);
    }
}
