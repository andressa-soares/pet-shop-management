package com.br.pet_shop_management.infrastructure.persistence;

import com.br.pet_shop_management.domain.entity.AppointmentEntity;
import com.br.pet_shop_management.domain.enums.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
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
    select a from AppointmentEntity a where a.scheduledAt > :now
      and a.status in :statuses order by a.scheduledAt asc""")
    Page<AppointmentEntity> findFutureByStatuses(
            @Param("now") LocalDateTime now,
            @Param("statuses") List<AppointmentStatus> statuses,
            Pageable pageable);

    @EntityGraph(attributePaths = {"owner", "pet"})
    @Query("""
    select a from AppointmentEntity a where a.scheduledAt < :now
      and a.status in :statuses order by a.scheduledAt desc""")
    Page<AppointmentEntity> findHistoryByStatuses(
            @Param("now") LocalDateTime now,
            @Param("statuses") List<AppointmentStatus> statuses,
            Pageable pageable);

    boolean existsByPetIdAndScheduledAtAndStatusIn(Long petId, LocalDateTime scheduledAt, List<AppointmentStatus> statuses);
    boolean existsByOwnerIdAndStatusIn(Long ownerId, List<AppointmentStatus> statuses);
}