package com.br.pet_shop_management.infrastructure.persistence;

import com.br.pet_shop_management.domain.entity.AppointmentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    @EntityGraph(attributePaths = {"owner", "pet"})
    Optional<AppointmentEntity> findDetailedById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"owner", "pet"})
    @Query("select a from AppointmentEntity a where a.id = :id")
    Optional<AppointmentEntity> findDetailedByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"owner", "pet"})
    @Query("""
    select a from AppointmentEntity a where a.scheduledAt > :now and a.status in (
        com.br.pet_shop_management.domain.enums.AppointmentStatus.SCHEDULED,
        com.br.pet_shop_management.domain.enums.AppointmentStatus.IN_PROGRESS,
        com.br.pet_shop_management.domain.enums.AppointmentStatus.WAITING_PAYMENT)
    """)
    Page<AppointmentEntity> findFutureActive(@Param("now") LocalDateTime now, Pageable pageable);
}