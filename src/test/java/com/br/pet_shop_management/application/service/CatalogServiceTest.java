package com.br.pet_shop_management.application.service;

import com.br.pet_shop_management.api.dto.request.CatalogForm;
import com.br.pet_shop_management.api.dto.request.enums.CatalogAction;
import com.br.pet_shop_management.api.dto.response.CatalogDTO;
import com.br.pet_shop_management.application.exception.DomainRuleException;
import com.br.pet_shop_management.application.exception.InvalidInputException;
import com.br.pet_shop_management.domain.entity.CatalogEntity;
import com.br.pet_shop_management.domain.enums.Status;
import com.br.pet_shop_management.infrastructure.persistence.CatalogRepository;
import com.br.pet_shop_management.infrastructure.persistence.OwnerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private CatalogService catalogService;

    private static CatalogForm validFormWithName(String name) {
        return new CatalogForm(
                name,
                "desc",
                30,
                new BigDecimal("10.00"),
                new BigDecimal("15.00"),
                new BigDecimal("20.00")
        );
    }

    private static CatalogEntity anyEntity() {
        return new CatalogEntity(
                "Banho",
                "desc",
                30,
                new BigDecimal("10.00"),
                new BigDecimal("15.00"),
                new BigDecimal("20.00")
        );
    }

    // ---------- findCatalogItems ----------

    @Test
    void findCatalogItems_shouldDefaultToActiveWhenStatusNull() {
        Pageable pageable = PageRequest.of(0, 10);

        when(catalogRepository.findByStatus(eq(Status.ACTIVE), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(anyEntity()), pageable, 1));

        Page<CatalogDTO> result = catalogService.findCatalogItems(null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(catalogRepository).findByStatus(Status.ACTIVE, pageable);
    }

    @Test
    void findCatalogItems_shouldUseProvidedStatus() {
        Pageable pageable = PageRequest.of(0, 10);

        when(catalogRepository.findByStatus(eq(Status.INACTIVE), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        Page<CatalogDTO> result = catalogService.findCatalogItems(Status.INACTIVE, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isZero();

        verify(catalogRepository).findByStatus(Status.INACTIVE, pageable);
    }

    // ---------- findById ----------

    @Test
    void findById_shouldReturnDtoWhenFound() {
        when(catalogRepository.findById(1L)).thenReturn(Optional.of(anyEntity()));

        CatalogDTO dto = catalogService.findById(1L);

        assertThat(dto).isNotNull();
        verify(catalogRepository).findById(1L);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(catalogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Catalog item not found.");

        verify(catalogRepository).findById(99L);
    }

    // ---------- saveCatalogItem ----------

    @Test
    void saveCatalogItem_shouldThrowWhenFormNull() {
        assertThatThrownBy(() -> catalogService.saveCatalogItem(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Catalog form must be provided.");

        verifyNoInteractions(catalogRepository);
    }

    @Test
    void saveCatalogItem_shouldThrowWhenNameBlank() {
        CatalogForm form = validFormWithName("   ");

        assertThatThrownBy(() -> catalogService.saveCatalogItem(form))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Name is required.");

        verifyNoInteractions(catalogRepository);
    }

    @Test
    void saveCatalogItem_shouldThrowWhenDuplicateName() {
        CatalogForm form = validFormWithName("Banho");

        when(catalogRepository.existsByName("Banho")).thenReturn(true);

        assertThatThrownBy(() -> catalogService.saveCatalogItem(form))
                .isInstanceOf(DomainRuleException.class)
                .hasMessage("Catalog item with this name already exists.");

        verify(catalogRepository).existsByName("Banho");
        verify(catalogRepository, never()).save(any());
    }

    @Test
    void saveCatalogItem_shouldSaveWhenValid() {
        CatalogForm form = validFormWithName("Banho");

        when(catalogRepository.existsByName("Banho")).thenReturn(false);
        when(catalogRepository.save(any(CatalogEntity.class))).thenReturn(anyEntity());

        CatalogDTO dto = catalogService.saveCatalogItem(form);

        assertThat(dto).isNotNull();
        verify(catalogRepository).existsByName("Banho");
        verify(catalogRepository).save(any(CatalogEntity.class));
    }

    // ---------- applyAction ----------

    private static CatalogEntity entityActive() {
        CatalogEntity e = anyEntity();
        e.activate(); // garante ACTIVE
        return e;
    }

    private static CatalogEntity entityInactive() {
        CatalogEntity e = anyEntity();
        e.deactivate(); // garante INACTIVE
        return e;
    }

    @Test
    void applyAction_shouldThrowWhenActionNull() {
        assertThatThrownBy(() -> catalogService.applyAction(1L, null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Action must be provided.");

        verifyNoInteractions(catalogRepository);
    }

    @Test
    void applyAction_shouldThrowWhenIdNull() {
        assertThatThrownBy(() -> catalogService.applyAction(null, CatalogAction.ACTIVATE))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Catalog ID must be provided.");

        verifyNoInteractions(catalogRepository);
    }

    @Test
    void applyAction_shouldThrowWhenCatalogNotFound() {
        when(catalogRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.applyAction(1L, CatalogAction.ACTIVATE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Catalog item not found.");

        verify(catalogRepository).findById(1L);
        verify(catalogRepository, never()).save(any());
    }

    @Test
    void applyAction_activate_shouldSaveWhenInactive() {
        CatalogEntity entity = entityInactive();

        when(catalogRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(catalogRepository.save(any(CatalogEntity.class))).thenReturn(entity);

        CatalogDTO dto = catalogService.applyAction(1L, CatalogAction.ACTIVATE);

        assertThat(dto).isNotNull();
        verify(catalogRepository).findById(1L);
        verify(catalogRepository).save(entity);
    }

    @Test
    void applyAction_activate_shouldThrowWhenAlreadyActive() {
        CatalogEntity entity = entityActive();

        when(catalogRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> catalogService.applyAction(1L, CatalogAction.ACTIVATE))
                .isInstanceOf(DomainRuleException.class)
                .hasMessage("Catalog item is already active.");

        verify(catalogRepository).findById(1L);
        verify(catalogRepository, never()).save(any());
    }

    @Test
    void applyAction_deactivate_shouldSaveWhenActive() {
        CatalogEntity entity = entityActive();

        when(catalogRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(catalogRepository.save(any(CatalogEntity.class))).thenReturn(entity);

        CatalogDTO dto = catalogService.applyAction(1L, CatalogAction.DEACTIVATE);

        assertThat(dto).isNotNull();
        verify(catalogRepository).findById(1L);
        verify(catalogRepository).save(entity);
    }

    @Test
    void applyAction_deactivate_shouldThrowWhenAlreadyInactive() {
        CatalogEntity entity = entityInactive();

        when(catalogRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> catalogService.applyAction(1L, CatalogAction.DEACTIVATE))
                .isInstanceOf(DomainRuleException.class)
                .hasMessage("Catalog item is already inactive.");

        verify(catalogRepository).findById(1L);
        verify(catalogRepository, never()).save(any());
    }


    // ---------- deleteCatalogItem ----------

    @Test
    void deleteCatalogItem_shouldThrowWhenIdNull() {
        assertThatThrownBy(() -> catalogService.deleteCatalogItem(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Catalog ID must be provided.");

        verifyNoInteractions(catalogRepository);
    }

    @Test
    void deleteCatalogItem_shouldDeleteWhenFound() {
        CatalogEntity entity = anyEntity();
        when(catalogRepository.findById(1L)).thenReturn(Optional.of(entity));

        catalogService.deleteCatalogItem(1L);

        verify(catalogRepository).findById(1L);
        verify(catalogRepository).delete(entity);
    }

    @Test
    void deleteCatalogItem_shouldThrowWhenNotFound() {
        when(catalogRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.deleteCatalogItem(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Catalog item not found.");

        verify(catalogRepository).findById(1L);
        verify(catalogRepository, never()).delete(any());
    }
}