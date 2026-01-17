package com.br.pet_shop_management.infrastructure.persistence;

import com.br.pet_shop_management.domain.entity.CatalogEntity;
import com.br.pet_shop_management.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogRepository extends JpaRepository<CatalogEntity, Long> {
    Page<CatalogEntity> findByStatus(Status status, Pageable pageable);
    boolean existsByName(String name);
}