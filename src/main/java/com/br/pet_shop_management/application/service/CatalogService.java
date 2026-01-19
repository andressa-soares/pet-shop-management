package com.br.pet_shop_management.application.service;

import com.br.pet_shop_management.api.dto.request.CatalogForm;
import com.br.pet_shop_management.api.dto.request.enums.CatalogAction;
import com.br.pet_shop_management.api.dto.response.CatalogDTO;
import com.br.pet_shop_management.application.exception.DomainRuleException;
import com.br.pet_shop_management.application.exception.InvalidInputException;
import com.br.pet_shop_management.application.mapper.CatalogMapper;
import com.br.pet_shop_management.domain.entity.CatalogEntity;
import com.br.pet_shop_management.domain.enums.Status;
import com.br.pet_shop_management.infrastructure.persistence.CatalogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogRepository catalogRepository;

    public Page<CatalogDTO> findCatalogItems(Status status, Pageable pageable) {
        Status effectiveStatus = (status == null) ? Status.ACTIVE : status;
        return catalogRepository.findByStatus(effectiveStatus, pageable).map(CatalogMapper::toDTO);
    }

    public CatalogDTO findById(Long id) {
        CatalogEntity item = catalogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catalog item not found."));
        return CatalogMapper.toDTO(item);
    }

    public CatalogDTO saveCatalogItem(CatalogForm form) {
        String name = normalizeName(form.name());

        // Ideal: existsByNameIgnoreCase ou unique index; mantendo simples:
        if (catalogRepository.existsByName(name)) {
            throw new DomainRuleException("Catalog item with this name already exists.");
        }

        CatalogEntity entity = new CatalogEntity(
                name,
                form.description(),
                form.durationMinutes(),
                form.priceSmall(),
                form.priceMedium(),
                form.priceLarge()
        );

        return CatalogMapper.toDTO(catalogRepository.save(entity));
    }

    public CatalogDTO applyAction(Long id, CatalogAction action) {
        CatalogEntity item = findCatalogEntity(id);

        if (action == CatalogAction.ACTIVATE) {
            if (item.getStatus() == Status.ACTIVE) throw new DomainRuleException("Catalog item is already active.");
            item.activate();
            return CatalogMapper.toDTO(catalogRepository.save(item));
        }

        if (action == CatalogAction.DEACTIVATE) {
            if (item.getStatus() == Status.INACTIVE) throw new DomainRuleException("Catalog item is already inactive.");
            item.deactivate();
            return CatalogMapper.toDTO(catalogRepository.save(item));
        }

        throw new InvalidInputException("Unsupported action.");
    }

    public void deleteCatalogItem(Long id) {
        CatalogEntity item = findCatalogEntity(id);
        catalogRepository.delete(item);
    }

    private CatalogEntity findCatalogEntity(Long id) {
        if (id == null) throw new InvalidInputException("Catalog ID must be provided.");
        return catalogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catalog item not found."));
    }

    private String normalizeName(String raw) {
        if (raw == null) throw new InvalidInputException("Name is required.");
        String name = raw.trim();
        if (name.isBlank()) throw new InvalidInputException("Name is required.");
        return name;
    }
}