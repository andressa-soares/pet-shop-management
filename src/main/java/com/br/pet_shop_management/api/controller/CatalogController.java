package com.br.pet_shop_management.api.controller;

import com.br.pet_shop_management.api.dto.request.CatalogActionForm;
import com.br.pet_shop_management.api.dto.request.CatalogForm;
import com.br.pet_shop_management.api.dto.response.CatalogDTO;
import com.br.pet_shop_management.application.service.CatalogService;
import com.br.pet_shop_management.domain.enums.Status;
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
    public Page<CatalogDTO> findCatalogItems(@RequestParam(required = false) Status status, Pageable pageable) {
        return catalogService.findCatalogItems(status, pageable);
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

    @PostMapping("/{id:\\d+}/actions")
    public CatalogDTO applyAction(@PathVariable Long id, @Valid @RequestBody CatalogActionForm form) {
        return catalogService.applyAction(id, form.action());
    }

    @DeleteMapping("/{id:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCatalogItem(@PathVariable Long id) {
        catalogService.deleteCatalogItem(id);
    }
}