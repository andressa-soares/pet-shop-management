package com.br.pet_shop_management.api.controller;

import com.br.pet_shop_management.api.dto.request.CatalogForm;
import com.br.pet_shop_management.api.dto.response.CatalogDTO;
import com.br.pet_shop_management.application.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public Page<CatalogDTO> findCatalogItems(@RequestParam(required = false) Boolean active, Pageable pageable) {
        return catalogService.findCatalogItems(active, pageable);
    }

    @GetMapping("/{id:\\d+}")
    public CatalogDTO findById(@PathVariable Long id) {
        return catalogService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogDTO saveCatalogItem(@Valid @RequestBody CatalogForm form) {
        return catalogService.saveCatalogItem(form);
    }

    @PatchMapping("/{id:\\d+}/activate")
    public CatalogDTO activateCatalogItem(@PathVariable Long id) {
        return catalogService.activateCatalogItem(id);
    }

    @PatchMapping("/{id}/deactivate")
    public CatalogDTO deactivateCatalogItem(@PathVariable Long id) {
        return catalogService.deactivateCatalogItem(id);
    }

    @DeleteMapping("/{id}/delete")
    public CatalogDTO deleteCatalogItem(@PathVariable Long id) {
        return catalogService.deleteCatalogItem(id);
    }
}