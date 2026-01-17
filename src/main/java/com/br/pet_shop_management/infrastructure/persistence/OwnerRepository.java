package com.br.pet_shop_management.infrastructure.persistence;

import com.br.pet_shop_management.domain.entity.OwnerEntity;
import com.br.pet_shop_management.domain.enums.OwnerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OwnerRepository extends JpaRepository<OwnerEntity, Long> {
    boolean existsByCpf(String cpf);
    Optional<OwnerEntity> findByCpf(String cpf);
    Page<OwnerEntity> findByStatus(OwnerStatus status, Pageable pageable);
}