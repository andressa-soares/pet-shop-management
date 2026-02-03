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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogRepository catalogRepository;

    public Page<CatalogDTO> findCatalogItems(Status status, Pageable pageable) {
        Status effectiveStatus = (status == null) ? Status.ACTIVE : status;
        log.info("findCatalogItems: statusFilter={}, page={}", effectiveStatus, pageable);

        Page<CatalogDTO> result = catalogRepository.findByStatus(effectiveStatus, pageable)
                .map(CatalogMapper::toDTO);

        log.info("findCatalogItems completed: resultCount={}", result.getNumberOfElements());
        return result;
    }

    public CatalogDTO findById(Long id) {
        log.info("findCatalogById: catalogId={}", id);

        CatalogEntity item = catalogRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("findCatalogById failed: catalog item not found. catalogId={}", id);
                    return new EntityNotFoundException("Catalog item not found.");
                });

        log.info("findCatalogById completed: catalogId={}, status={}", item.getId(), item.getStatus());
        return CatalogMapper.toDTO(item);
    }

    public CatalogDTO saveCatalogItem(CatalogForm form) {
        log.info("saveCatalogItem started: name={}, durationMinutes={}",
                (form == null ? null : form.name()),
                (form == null ? null : form.durationMinutes()));

        if (form == null) {
            log.warn("saveCatalogItem invalid input: form is null");
            throw new InvalidInputException("Catalog form must be provided.");
        }

        String name = normalizeName(form.name());

        if (catalogRepository.existsByName(name)) {
            log.warn("saveCatalogItem blocked: duplicate name. name={}", name);
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

        CatalogEntity saved = catalogRepository.save(entity);

        log.info("saveCatalogItem completed: catalogId={}, name={}, status={}",
                saved.getId(), saved.getName(), saved.getStatus());

        return CatalogMapper.toDTO(saved);
    }

    public CatalogDTO applyAction(Long id, CatalogAction action) {
        log.info("applyCatalogAction started: catalogId={}, action={}", id, action);

        if (action == null) {
            log.warn("applyCatalogAction invalid input: action is null. catalogId={}", id);
            throw new InvalidInputException("Action must be provided.");
        }

        CatalogEntity item = findCatalogEntity(id);
        Status before = item.getStatus();

        if (action == CatalogAction.ACTIVATE) {
            if (item.getStatus() == Status.ACTIVE) {
                log.warn("applyCatalogAction blocked: already active. catalogId={}", item.getId());
                throw new DomainRuleException("Catalog item is already active.");
            }
            item.activate();
        } else if (action == CatalogAction.DEACTIVATE) {
            if (item.getStatus() == Status.INACTIVE) {
                log.warn("applyCatalogAction blocked: already inactive. catalogId={}", item.getId());
                throw new DomainRuleException("Catalog item is already inactive.");
            }
            item.deactivate();
        } else {
            log.warn("applyCatalogAction invalid input: unsupported action. catalogId={}, action={}", item.getId(), action);
            throw new InvalidInputException("Unsupported action.");
        }

        CatalogEntity updated = catalogRepository.save(item);

        log.info("applyCatalogAction completed: catalogId={}, statusBefore={}, statusAfter={}",
                updated.getId(), before, updated.getStatus());

        return CatalogMapper.toDTO(updated);
    }

    public void deleteCatalogItem(Long id) {
        log.info("deleteCatalogItem started: catalogId={}", id);

        CatalogEntity item = findCatalogEntity(id);
        catalogRepository.delete(item);

        log.info("deleteCatalogItem completed: catalogId={}", item.getId());
    }

    private CatalogEntity findCatalogEntity(Long id) {
        if (id == null) {
            log.warn("findCatalogEntity invalid input: id is null");
            throw new InvalidInputException("Catalog ID must be provided.");
        }

        return catalogRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("findCatalogEntity failed: catalog item not found. catalogId={}", id);
                    return new EntityNotFoundException("Catalog item not found.");
                });
    }

    private String normalizeName(String raw) {
        if (raw == null) throw new InvalidInputException("Name is required.");
        String name = raw.trim();
        if (name.isBlank()) throw new InvalidInputException("Name is required.");
        return name;
    }
}
