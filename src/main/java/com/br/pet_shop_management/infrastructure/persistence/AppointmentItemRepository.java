package com.br.pet_shop_management.infrastructure.persistence;

import com.br.pet_shop_management.domain.entity.AppointmentItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentItemRepository extends JpaRepository<AppointmentItemEntity, Long> {
    List<AppointmentItemEntity> findByAppointmentId(Long appointmentId);
}