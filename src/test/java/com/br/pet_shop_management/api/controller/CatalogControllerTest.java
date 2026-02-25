package com.br.pet_shop_management.api.controller;

import com.br.pet_shop_management.api.dto.response.CatalogDTO;
import com.br.pet_shop_management.application.service.CatalogService;
import com.br.pet_shop_management.domain.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CatalogController.class)
class CatalogControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CatalogService catalogService;

    private static CatalogDTO dtoActive() {
        return new CatalogDTO(
                1L, "Banho", "desc", 30,
                new BigDecimal("10.00"),
                new BigDecimal("15.00"),
                new BigDecimal("20.00"),
                Status.ACTIVE
        );
    }

    @Test
    void findById_shouldReturn200AndBody() throws Exception {
        when(catalogService.findById(1L)).thenReturn(dtoActive());

        mockMvc.perform(get("/catalog/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Banho"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(catalogService).findById(1L);
        verifyNoMoreInteractions(catalogService);
    }

    @Test
    void findCatalogItems_shouldReturn200AndPageContent() throws Exception {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<CatalogDTO> page = new PageImpl<>(List.of(dtoActive()), pageable, 1);

        when(catalogService.findCatalogItems(eq(Status.ACTIVE), any())).thenReturn(page);

        mockMvc.perform(get("/catalog")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(catalogService).findCatalogItems(eq(Status.ACTIVE), any());
        verifyNoMoreInteractions(catalogService);
    }

    @Test
    void saveCatalogItem_shouldReturn201() throws Exception {
        when(catalogService.saveCatalogItem(any())).thenReturn(dtoActive());

        // CatalogForm: name, description, durationMinutes, priceSmall, priceMedium, priceLarge
        String body = """
                {
                  "name": "Banho",
                  "description": "desc",
                  "durationMinutes": 30,
                  "priceSmall": 10.00,
                  "priceMedium": 15.00,
                  "priceLarge": 20.00
                }
                """;

        mockMvc.perform(post("/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Banho"));

        verify(catalogService).saveCatalogItem(any());
        verifyNoMoreInteractions(catalogService);
    }

    @Test
    void applyAction_shouldReturn200() throws Exception {
        when(catalogService.applyAction(1L, com.br.pet_shop_management.api.dto.request.enums.CatalogAction.ACTIVATE))
                .thenReturn(dtoActive());

        String body = """
                {"action":"ACTIVATE"}
                """;

        mockMvc.perform(post("/catalog/1/actions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(catalogService).applyAction(1L, com.br.pet_shop_management.api.dto.request.enums.CatalogAction.ACTIVATE);
        verifyNoMoreInteractions(catalogService);
    }

    @Test
    void deleteCatalogItem_shouldReturn204() throws Exception {
        doNothing().when(catalogService).deleteCatalogItem(1L);

        mockMvc.perform(delete("/catalog/1"))
                .andExpect(status().isNoContent());

        verify(catalogService).deleteCatalogItem(1L);
        verifyNoMoreInteractions(catalogService);
    }
}