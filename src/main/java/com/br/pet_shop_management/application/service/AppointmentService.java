package com.br.pet_shop_management.application.service;

import com.br.pet_shop_management.api.dto.request.AppointmentForm;
import com.br.pet_shop_management.api.dto.request.AppointmentItemForm;
import com.br.pet_shop_management.api.dto.request.enums.AppointmentAction;
import com.br.pet_shop_management.api.dto.response.AppointmentDTO;
import com.br.pet_shop_management.application.exception.DomainRuleException;
import com.br.pet_shop_management.application.exception.InvalidInputException;
import com.br.pet_shop_management.application.mapper.AppointmentMapper;
import com.br.pet_shop_management.domain.entity.*;
import com.br.pet_shop_management.domain.enums.AppointmentStatus;
import com.br.pet_shop_management.domain.enums.Status;
import com.br.pet_shop_management.infrastructure.persistence.*;
import com.br.pet_shop_management.util.MoneyUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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
        log.info("createAppointment started: ownerId={}, petId={}, scheduledAt={}, itemsCount={}",
                form.ownerId(), form.petId(), form.scheduledAt(),
                (form.items() == null ? 0 : form.items().size()));

        OwnerEntity owner = ownerRepository.findById(form.ownerId())
                .orElseThrow(() -> {
                    log.warn("createAppointment failed: owner not found. ownerId={}", form.ownerId());
                    return new EntityNotFoundException("Owner not found.");
                });

        if (owner.getStatus() == Status.INACTIVE) {
            log.warn("createAppointment blocked: inactive owner. ownerId={}", owner.getId());
            throw new DomainRuleException("Inactive owners cannot create appointments.");
        }

        PetEntity pet = petRepository.findById(form.petId())
                .orElseThrow(() -> {
                    log.warn("createAppointment failed: pet not found. petId={}", form.petId());
                    return new EntityNotFoundException("Pet not found.");
                });

        if (!pet.getOwner().getId().equals(owner.getId())) {
            log.warn("createAppointment blocked: pet does not belong to owner. ownerId={}, petId={}, petOwnerId={}",
                    owner.getId(), pet.getId(), pet.getOwner().getId());
            throw new DomainRuleException("Pet does not belong to the provided owner.");
        }

        if (form.scheduledAt() == null) {
            log.warn("createAppointment invalid input: scheduledAt is null. ownerId={}, petId={}", owner.getId(), pet.getId());
            throw new InvalidInputException("Scheduled date/time is required.");
        }

        if (form.scheduledAt().isBefore(LocalDateTime.now())) {
            log.warn("createAppointment blocked: scheduledAt in the past. ownerId={}, petId={}, scheduledAt={}",
                    owner.getId(), pet.getId(), form.scheduledAt());
            throw new DomainRuleException("Scheduled date/time cannot be in the past.");
        }

        if (form.items() == null || form.items().isEmpty()) {
            log.warn("createAppointment invalid input: empty items. ownerId={}, petId={}", owner.getId(), pet.getId());
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
            log.warn("createAppointment blocked: scheduling conflict. petId={}, scheduledAt={}", pet.getId(), form.scheduledAt());
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

        log.info("createAppointment completed: appointmentId={}, status={}, totalGross={}, itemsCount={}",
                updated.getId(), updated.getStatus(), updated.getTotalGross(), items.size());

        return AppointmentMapper.toDTO(updated, items);
    }

    @Transactional
    public AppointmentDTO addAppointmentItems(Long appointmentId, List<AppointmentItemForm> newItems) {
        log.info("addAppointmentItems started: appointmentId={}, newItemsCount={}",
                appointmentId, (newItems == null ? 0 : newItems.size()));

        if (appointmentId == null) {
            log.warn("addAppointmentItems invalid input: appointmentId is null");
            throw new InvalidInputException("Appointment ID must be provided.");
        }

        if (newItems == null || newItems.isEmpty()) {
            log.warn("addAppointmentItems invalid input: empty items. appointmentId={}", appointmentId);
            throw new InvalidInputException("At least one service item must be provided.");
        }

        AppointmentEntity appointment = appointmentRepository.findDetailedByIdForUpdate(appointmentId)
                .orElseThrow(() -> {
                    log.warn("addAppointmentItems failed: appointment not found. appointmentId={}", appointmentId);
                    return new EntityNotFoundException("Appointment not found.");
                });

        if (appointment.isCanceled()) {
            log.warn("addAppointmentItems blocked: appointment canceled. appointmentId={}, status={}",
                    appointment.getId(), appointment.getStatus());
            throw new DomainRuleException("Canceled appointments cannot be modified.");
        }

        if (appointment.isLocked()) {
            log.warn("addAppointmentItems blocked: appointment locked. appointmentId={}, status={}",
                    appointment.getId(), appointment.getStatus());
            throw new DomainRuleException("Appointments waiting for payment cannot be modified.");
        }

        if (appointment.getOwner().getStatus() == Status.INACTIVE) {
            log.warn("addAppointmentItems blocked: owner inactive. appointmentId={}, ownerId={}",
                    appointment.getId(), appointment.getOwner().getId());
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

        log.info("addAppointmentItems completed: appointmentId={}, totalGross={}, totalItemsCount={}",
                updated.getId(), updated.getTotalGross(), allItems.size());

        return AppointmentMapper.toDTO(updated, allItems);
    }

    @Transactional(readOnly = true)
    public AppointmentDTO findById(Long id) {
        log.info("findAppointmentById: appointmentId={}", id);

        AppointmentEntity appointment = appointmentRepository.findDetailedById(id)
                .orElseThrow(() -> {
                    log.warn("findAppointmentById failed: appointment not found. appointmentId={}", id);
                    return new EntityNotFoundException("Appointment not found.");
                });

        List<AppointmentItemEntity> items = appointmentItemRepository.findByAppointmentId(id);

        log.info("findAppointmentById completed: appointmentId={}, status={}, itemsCount={}",
                appointment.getId(), appointment.getStatus(), items.size());

        return AppointmentMapper.toDTO(appointment, items);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentDTO> listFutureAppointments(AppointmentStatus status, Pageable pageable) {
        log.info("listFutureAppointments started: statusFilter={}, page={}", status, pageable);

        List<AppointmentStatus> statuses;
        if (status == null) {
            statuses = List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.IN_PROGRESS, AppointmentStatus.WAITING_PAYMENT);
        } else {
            if (status == AppointmentStatus.CANCELED || status == AppointmentStatus.COMPLETED) {
                log.warn("listFutureAppointments invalid status filter: {}", status);
                throw new InvalidInputException("Status filter must be an active status for future appointments.");
            }
            statuses = List.of(status);
        }

        Page<AppointmentEntity> page =
                appointmentRepository.findFutureByStatuses(LocalDateTime.now(), statuses, pageable);

        if (page.isEmpty()) {
            log.info("listFutureAppointments completed: empty result. statusFilter={}", status);
            return Page.empty(pageable);
        }

        List<Long> ids = page.getContent().stream().map(AppointmentEntity::getId).toList();
        List<AppointmentItemEntity> items = appointmentItemRepository.findByAppointmentIdIn(ids);

        var itemsByAppointmentId =
                items.stream().collect(java.util.stream.Collectors.groupingBy(i -> i.getAppointment().getId()));

        log.info("listFutureAppointments completed: resultCount={}", page.getNumberOfElements());

        return page.map(a -> AppointmentMapper.toDTO(a, itemsByAppointmentId.getOrDefault(a.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public Page<AppointmentDTO> listHistory(AppointmentStatus status, Pageable pageable) {
        log.info("listHistory started: statusFilter={}, page={}", status, pageable);

        List<AppointmentStatus> statuses = (status == null)
                ? List.of(AppointmentStatus.values())
                : List.of(status);

        Page<AppointmentEntity> page =
                appointmentRepository.findHistoryByStatuses(LocalDateTime.now(), statuses, pageable);

        if (page.isEmpty()) {
            log.info("listHistory completed: empty result. statusFilter={}", status);
            return Page.empty(pageable);
        }

        List<Long> ids = page.getContent().stream().map(AppointmentEntity::getId).toList();
        List<AppointmentItemEntity> items = appointmentItemRepository.findByAppointmentIdIn(ids);

        var itemsByAppointmentId =
                items.stream().collect(java.util.stream.Collectors.groupingBy(i -> i.getAppointment().getId()));

        log.info("listHistory completed: resultCount={}", page.getNumberOfElements());

        return page.map(a -> AppointmentMapper.toDTO(a, itemsByAppointmentId.getOrDefault(a.getId(), List.of())));
    }

    @Transactional
    public AppointmentDTO applyAction(Long id, AppointmentAction action) {
        log.info("applyAppointmentAction started: appointmentId={}, action={}", id, action);

        if (id == null) {
            log.warn("applyAppointmentAction invalid input: appointmentId is null");
            throw new InvalidInputException("Appointment ID must be provided.");
        }

        if (action == null) {
            log.warn("applyAppointmentAction invalid input: action is null. appointmentId={}", id);
            throw new InvalidInputException("Action must be provided.");
        }

        AppointmentEntity appointment = appointmentRepository.findDetailedByIdForUpdate(id)
                .orElseThrow(() -> {
                    log.warn("applyAppointmentAction failed: appointment not found. appointmentId={}", id);
                    return new EntityNotFoundException("Appointment not found.");
                });

        if (appointment.getOwner().getStatus() == Status.INACTIVE) {
            log.warn("applyAppointmentAction blocked: owner inactive. appointmentId={}, ownerId={}",
                    appointment.getId(), appointment.getOwner().getId());
            throw new DomainRuleException("Appointments from inactive owners cannot be updated.");
        }

        AppointmentStatus before = appointment.getStatus();

        try {
            switch (action) {
                case START -> {
                    if (appointment.isCanceled()) {
                        log.warn("applyAppointmentAction blocked: cannot start canceled appointment. appointmentId={}", appointment.getId());
                        throw new DomainRuleException("Canceled appointments cannot be started.");
                    }
                    if (appointment.isLocked()) {
                        log.warn("applyAppointmentAction blocked: cannot start locked appointment. appointmentId={}, status={}",
                                appointment.getId(), appointment.getStatus());
                        throw new DomainRuleException("Appointments waiting for payment or completed cannot be started.");
                    }
                    appointment.start();
                }
                case CLOSE_FOR_PAYMENT -> {
                    if (appointment.isLocked()) {
                        log.warn("applyAppointmentAction blocked: already locked. appointmentId={}, status={}",
                                appointment.getId(), appointment.getStatus());
                        throw new DomainRuleException("Appointment is already waiting for payment or completed.");
                    }
                    if (appointment.isCanceled()) {
                        log.warn("applyAppointmentAction blocked: cannot close canceled appointment. appointmentId={}", appointment.getId());
                        throw new DomainRuleException("Canceled appointments cannot be closed for payment.");
                    }
                    appointment.closeForPayment(LocalDateTime.now());
                }
                case CANCEL -> appointment.cancel();
            }
        } catch (IllegalStateException e) {
            log.warn("applyAppointmentAction blocked by state rule: appointmentId={}, action={}, statusBefore={}, msg={}",
                    appointment.getId(), action, before, e.getMessage());
            throw new DomainRuleException(e.getMessage());
        }

        AppointmentEntity updated = appointmentRepository.save(appointment);
        List<AppointmentItemEntity> items = appointmentItemRepository.findByAppointmentId(updated.getId());

        log.info("applyAppointmentAction completed: appointmentId={}, action={}, statusBefore={}, statusAfter={}",
                updated.getId(), action, before, updated.getStatus());

        return AppointmentMapper.toDTO(updated, items);
    }

    private List<AppointmentItemEntity> buildItems(AppointmentEntity appointment, PetEntity pet, List<AppointmentItemForm> forms) {
        return forms.stream().map(itemForm -> {
            if (itemForm.catalogId() == null) {
                log.warn("buildItems invalid input: catalogId is null. appointmentId={}", appointment.getId());
                throw new InvalidInputException("Catalog ID is required.");
            }

            CatalogEntity catalog = catalogRepository.findById(itemForm.catalogId())
                    .orElseThrow(() -> {
                        log.warn("buildItems failed: catalog item not found. catalogId={}, appointmentId={}",
                                itemForm.catalogId(), appointment.getId());
                        return new EntityNotFoundException("Catalog item not found.");
                    });

            if (catalog.getStatus() == Status.INACTIVE) {
                log.warn("buildItems blocked: inactive catalog item used. catalogId={}, appointmentId={}",
                        catalog.getId(), appointment.getId());
                throw new DomainRuleException("Inactive catalog items cannot be used.");
            }

            if (itemForm.quantity() == null || itemForm.quantity() < 1) {
                log.warn("buildItems invalid input: invalid quantity. quantity={}, catalogId={}, appointmentId={}",
                        itemForm.quantity(), catalog.getId(), appointment.getId());
                throw new InvalidInputException("Quantity must be at least 1.");
            }

            BigDecimal unitPrice = MoneyUtils.scale(catalog.getPriceByPetSize(pet.getSize()));
            if (unitPrice == null || unitPrice.signum() <= 0) {
                log.warn("buildItems blocked: invalid price for pet size. catalogId={}, petSize={}, appointmentId={}",
                        catalog.getId(), pet.getSize(), appointment.getId());
                throw new DomainRuleException("Invalid catalog price for pet size.");
            }

            return AppointmentItemEntity.create(appointment, catalog, itemForm.quantity(), unitPrice);
        }).toList();
    }
}
