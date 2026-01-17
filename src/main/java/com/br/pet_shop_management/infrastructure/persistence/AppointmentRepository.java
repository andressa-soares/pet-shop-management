package com.br.pet_shop_management.infrastructure.persistence;

import com.br.pet_shop_management.domain.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    @EntityGraph(attributePaths = {"owner", "pet"})
    Optional<AppointmentEntity> findDetailedById(Long id);
}