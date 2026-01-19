package com.br.pet_shop_management.application.service;

import com.br.pet_shop_management.api.dto.request.enums.AppointmentAction;
import com.br.pet_shop_management.application.exception.DomainRuleException;
import com.br.pet_shop_management.application.exception.InvalidInputException;
import com.br.pet_shop_management.domain.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.br.pet_shop_management.api.dto.request.AppointmentForm;
import com.br.pet_shop_management.api.dto.request.AppointmentItemForm;
import com.br.pet_shop_management.api.dto.response.AppointmentDTO;
import com.br.pet_shop_management.application.mapper.AppointmentMapper;
import com.br.pet_shop_management.domain.entity.*;
import com.br.pet_shop_management.domain.enums.Status;
import com.br.pet_shop_management.infrastructure.persistence.*;
import com.br.pet_shop_management.util.MoneyUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentItemRepository appointmentItemRepository;
    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final CatalogRepository catalogRepository;

    @Transactional
    public AppointmentDTO createAppointment(AppointmentForm form) {
        OwnerEntity owner = ownerRepository.findById(form.ownerId())
                .orElseThrow(() -> new EntityNotFoundException("Owner not found."));

        if (owner.getStatus() == Status.INACTIVE) {
            throw new DomainRuleException("Inactive owners cannot create appointments.");
        }

        PetEntity pet = petRepository.findById(form.petId())
                .orElseThrow(() -> new EntityNotFoundException("Pet not found."));

        if (!pet.getOwner().getId().equals(owner.getId())) {
            throw new DomainRuleException("Pet does not belong to the provided owner.");
        }

        if (form.scheduledAt().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException("Scheduled date/time cannot be in the past.");
        }

        if (form.items() == null || form.items().isEmpty()) {
            throw new InvalidInputException("At least one service item must be provided.");
        }

        List<AppointmentStatus> activeStatuses = List.of(
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.IN_PROGRESS,
                AppointmentStatus.WAITING_PAYMENT
        );

        boolean hasConflict = appointmentRepository.existsByPetIdAndScheduledAtAndStatusIn(
                pet.getId(),
                form.scheduledAt(),
                activeStatuses
        );

        if (hasConflict) {
            throw new DomainRuleException("This pet already has an appointment scheduled for the same date/time.");
        }

        AppointmentEntity appointment = new AppointmentEntity(owner, pet, form.scheduledAt());
        AppointmentEntity savedAppointment = appointmentRepository.save(appointment);

        List<AppointmentItemEntity> items = buildItems(savedAppointment, pet, form.items());
        appointmentItemRepository.saveAll(items);

        BigDecimal totalGross = MoneyUtils.scale(
                items.stream()
                        .map(AppointmentItemEntity::getSubtotal)
                        .reduce(MoneyUtils.zero(), BigDecimal::add)
        );

        savedAppointment.updateTotalGross(totalGross);
        AppointmentEntity updated = appointmentRepository.save(savedAppointment);

        return AppointmentMapper.toDTO(updated, items);
    }

    @Transactional
    public AppointmentDTO addAppointmentItems(Long appointmentId, List<AppointmentItemForm> newItems) {
        if (newItems == null || newItems.isEmpty()) {
            throw new InvalidInputException("At least one service item must be provided.");
        }

        AppointmentEntity appointment = appointmentRepository.findDetailedByIdForUpdate(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found."));

        if (appointment.isCanceled()) throw new DomainRuleException("Canceled appointments cannot be modified.");
        if (appointment.isLocked()) throw new DomainRuleException("Appointments waiting for payment cannot be modified.");

        if (appointment.getOwner().getStatus() == Status.INACTIVE) {
            throw new DomainRuleException("Appointments from inactive owners cannot be updated.");
        }

        PetEntity pet = appointment.getPet();

        List<AppointmentItemEntity> itemsToAdd = buildItems(appointment, pet, newItems);
        appointmentItemRepository.saveAll(itemsToAdd);

        List<AppointmentItemEntity> allItems = appointmentItemRepository.findByAppointmentId(appointmentId);

        BigDecimal totalGross = MoneyUtils.scale(
                allItems.stream()
                        .map(AppointmentItemEntity::getSubtotal)
                        .reduce(MoneyUtils.zero(), BigDecimal::add)
        );

        appointment.updateTotalGross(totalGross);
        AppointmentEntity updated = appointmentRepository.save(appointment);

        return AppointmentMapper.toDTO(updated, allItems);
    }

    @Transactional(readOnly = true)
    public AppointmentDTO findById(Long id) {
        AppointmentEntity appointment = appointmentRepository.findDetailedById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found."));

        List<AppointmentItemEntity> items = appointmentItemRepository.findByAppointmentId(id);
        return AppointmentMapper.toDTO(appointment, items);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentDTO> listFutureAppointments(AppointmentStatus status, Pageable pageable) {
        List<AppointmentStatus> statuses;
        if (status == null) {
            statuses = List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.IN_PROGRESS, AppointmentStatus.WAITING_PAYMENT);
        } else {
            if (status == AppointmentStatus.CANCELED || status == AppointmentStatus.COMPLETED) {
                throw new InvalidInputException("Status filter must be an active status for future appointments.");
            }
            statuses = List.of(status);
        }

        Page<AppointmentEntity> page = appointmentRepository.findFutureByStatuses(LocalDateTime.now(), statuses, pageable);
        if (page.isEmpty()) return Page.empty(pageable);

        var ids = page.getContent().stream().map(AppointmentEntity::getId).toList();
        var items = appointmentItemRepository.findByAppointmentIdIn(ids);

        var itemsByAppointmentId = items.stream()
                .collect(java.util.stream.Collectors.groupingBy(i -> i.getAppointment().getId()));

        return page.map(a -> AppointmentMapper.toDTO(a, itemsByAppointmentId.getOrDefault(a.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public Page<AppointmentDTO> listHistory(AppointmentStatus status, Pageable pageable) {
        List<AppointmentStatus> statuses = (status == null) ? List.of(AppointmentStatus.values()) : List.of(status);

        Page<AppointmentEntity> page = appointmentRepository.findHistoryByStatuses(LocalDateTime.now(), statuses, pageable);
        if (page.isEmpty()) return Page.empty(pageable);

        var ids = page.getContent().stream().map(AppointmentEntity::getId).toList();
        var items = appointmentItemRepository.findByAppointmentIdIn(ids);

        var itemsByAppointmentId = items.stream()
                .collect(java.util.stream.Collectors.groupingBy(i -> i.getAppointment().getId()));

        return page.map(a -> AppointmentMapper.toDTO(a, itemsByAppointmentId.getOrDefault(a.getId(), List.of())));
    }

    @Transactional
    public AppointmentDTO applyAction(Long id, AppointmentAction action) {
        AppointmentEntity appointment = appointmentRepository.findDetailedByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found."));

        if (appointment.getOwner().getStatus() == Status.INACTIVE) {
            throw new DomainRuleException("Appointments from inactive owners cannot be updated.");
        }

        try {
            switch (action) {

                case START -> {
                    if (appointment.isCanceled())
                        throw new DomainRuleException("Canceled appointments cannot be started.");

                    if (appointment.isLocked())
                        throw new DomainRuleException("Appointments waiting for payment or completed cannot be started.");

                    appointment.start();
                }

                case CLOSE_FOR_PAYMENT -> {
                    if (appointment.isLocked())
                        throw new DomainRuleException("Appointment is already waiting for payment or completed.");

                    if (appointment.isCanceled())
                        throw new DomainRuleException("Canceled appointments cannot be closed for payment.");

                    appointment.closeForPayment(LocalDateTime.now());
                }

                case CANCEL -> {
                    appointment.cancel();
                }
            }
        } catch (IllegalStateException e) {
            throw new DomainRuleException(e.getMessage());
        }

        AppointmentEntity updated = appointmentRepository.save(appointment);
        List<AppointmentItemEntity> items = appointmentItemRepository.findByAppointmentId(id);
        return AppointmentMapper.toDTO(updated, items);
    }

    private List<AppointmentItemEntity> buildItems(AppointmentEntity appointment, PetEntity pet, List<AppointmentItemForm> forms) {
        return forms.stream().map(itemForm -> {
            CatalogEntity catalog = catalogRepository.findById(itemForm.catalogId())
                    .orElseThrow(() -> new EntityNotFoundException("Catalog item not found."));

            if (catalog.getStatus() == Status.INACTIVE) {
                throw new DomainRuleException("Inactive catalog items cannot be used.");
            }

            // quantity: já validado no DTO (@NotNull, @Min(1)) e reforçado no Entity.
            BigDecimal unitPrice = MoneyUtils.scale(catalog.getPriceByPetSize(pet.getSize()));
            if (unitPrice == null || unitPrice.signum() <= 0) {
                throw new DomainRuleException("Invalid catalog price for pet size.");
            }

            return AppointmentItemEntity.create(appointment, catalog, itemForm.quantity(), unitPrice);
        }).toList();
    }
}