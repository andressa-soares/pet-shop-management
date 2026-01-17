package com.br.pet_shop_management.application.service;

import com.br.pet_shop_management.api.dto.request.CatalogForm;
import com.br.pet_shop_management.api.dto.response.CatalogDTO;
import com.br.pet_shop_management.application.exception.BusinessException;
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

        return catalogRepository.findByStatus(effectiveStatus, pageable)
                .map(CatalogMapper::toDTO);
    }

    public CatalogDTO findById(Long id) {
        CatalogEntity item = catalogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catalog item not found."));
        return CatalogMapper.toDTO(item);
    }

    public CatalogDTO saveCatalogItem(CatalogForm form) {
        if (catalogRepository.existsByName(form.name())) {
            throw new BusinessException("Catalog item with this name already exists.");
        }

        CatalogEntity entity = CatalogMapper.toEntity(form);
        return CatalogMapper.toDTO(catalogRepository.save(entity));
    }

    public CatalogDTO activateCatalogItem(Long id) {
        CatalogEntity item = findCatalogEntity(id);

        if (item.getStatus() == Status.ACTIVE) {
            throw new BusinessException("Catalog item is already active.");
        }

        item.activate();
        return CatalogMapper.toDTO(catalogRepository.save(item));
    }

    public CatalogDTO deactivateCatalogItem(Long id) {
        CatalogEntity item = findCatalogEntity(id);

        if (item.getStatus() == Status.INACTIVE) {
            throw new BusinessException("Catalog item is already inactive.");
        }

        item.deactivate();
        return CatalogMapper.toDTO(catalogRepository.save(item));
    }

    public void deleteCatalogItem(Long id) {
        CatalogEntity item = findCatalogEntity(id);
        catalogRepository.delete(item);
    }

    private CatalogEntity findCatalogEntity(Long id) {
        if (id == null) {
            throw new BusinessException("Catalog ID must be provided.");
        }
        return catalogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catalog item not found."));
    }
}