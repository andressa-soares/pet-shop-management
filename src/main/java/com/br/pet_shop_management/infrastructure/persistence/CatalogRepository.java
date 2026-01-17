package com.br.pet_shop_management.infrastructure.persistence;

import com.br.pet_shop_management.domain.entity.CatalogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogRepository extends JpaRepository<CatalogEntity, Long> {
    Page<CatalogEntity> findByActiveTrue(Pageable pageable);
    Page<CatalogEntity> findByActiveFalse(Pageable pageable);
    boolean existsByName(String name);
}