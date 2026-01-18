package com.br.pet_shop_management.infrastructure.persistence;

import com.br.pet_shop_management.domain.entity.AppointmentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    @EntityGraph(attributePaths = {"owner", "pet"})
    Optional<AppointmentEntity> findDetailedById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"owner", "pet"})
    @Query("select a from AppointmentEntity a where a.id = :id")
    Optional<AppointmentEntity> findDetailedByIdForUpdate(@Param("id") Long id);
}