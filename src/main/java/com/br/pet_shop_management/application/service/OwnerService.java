package com.br.pet_shop_management.application.service;

import com.br.pet_shop_management.api.dto.request.OwnerForm;
import com.br.pet_shop_management.api.dto.request.OwnerUpdateForm;
import com.br.pet_shop_management.api.dto.request.enums.OwnerAction;
import com.br.pet_shop_management.api.dto.response.OwnerDTO;
import com.br.pet_shop_management.application.exception.DomainRuleException;
import com.br.pet_shop_management.application.exception.InvalidInputException;
import com.br.pet_shop_management.application.mapper.OwnerMapper;
import com.br.pet_shop_management.domain.entity.OwnerEntity;
import com.br.pet_shop_management.domain.enums.AppointmentStatus;
import com.br.pet_shop_management.domain.enums.Status;
import com.br.pet_shop_management.infrastructure.persistence.AppointmentRepository;
import com.br.pet_shop_management.infrastructure.persistence.OwnerRepository;
import com.br.pet_shop_management.util.CpfUtils;
import com.br.pet_shop_management.util.PhoneUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final AppointmentRepository appointmentRepository;

    public Page<OwnerDTO> findAll(Pageable pageable) {
        log.info("findOwners started: status=ACTIVE, page={}", pageable);

        Page<OwnerDTO> result = ownerRepository.findByStatus(Status.ACTIVE, pageable)
                .map(OwnerMapper::toDTO);

        log.info("findOwners completed: resultCount={}", result.getNumberOfElements());
        return result;
    }

    public OwnerDTO findByCpf(String cpf) {
        String normalizedCpf = normalizeCpf(cpf);
        log.info("findOwnerByCpf: cpfNormalized={}", maskCpf(normalizedCpf));

        OwnerEntity owner = ownerRepository.findByCpf(normalizedCpf)
                .orElseThrow(() -> {
                    log.warn("findOwnerByCpf failed: owner not found. cpf={}", maskCpf(normalizedCpf));
                    return new EntityNotFoundException("Owner not found.");
                });

        log.info("findOwnerByCpf completed: ownerId={}, status={}", owner.getId(), owner.getStatus());
        return OwnerMapper.toDTO(owner);
    }

    public OwnerDTO findById(Long id) {
        log.info("findOwnerById: ownerId={}", id);

        OwnerEntity owner = ownerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("findOwnerById failed: owner not found. ownerId={}", id);
                    return new EntityNotFoundException("Owner not found.");
                });

        log.info("findOwnerById completed: ownerId={}, status={}", owner.getId(), owner.getStatus());
        return OwnerMapper.toDTO(owner);
    }

    public OwnerDTO saveOwner(OwnerForm ownerForm) {
        log.info("saveOwner started");

        if (ownerForm == null) {
            log.warn("saveOwner invalid input: form is null");
            throw new InvalidInputException("Owner form must be provided.");
        }

        String normalizedCpf = normalizeCpf(ownerForm.cpf());
        String normalizedPhone = normalizePhone(ownerForm.phone());

        if (ownerRepository.existsByCpf(normalizedCpf)) {
            log.warn("saveOwner blocked: duplicate CPF. cpf={}", maskCpf(normalizedCpf));
            throw new DomainRuleException("CPF already exists.");
        }

        OwnerEntity ownerEntity = OwnerMapper.toEntity(ownerForm, normalizedCpf, normalizedPhone, Status.ACTIVE);
        OwnerEntity saved = ownerRepository.save(ownerEntity);

        log.info("saveOwner completed: ownerId={}, status={}", saved.getId(), saved.getStatus());
        return OwnerMapper.toDTO(saved);
    }

    public OwnerDTO updateOwnerContact(String cpf, OwnerUpdateForm form) {
        String normalizedCpf = normalizeCpf(cpf);
        log.info("updateOwnerContact started: cpf={}", maskCpf(normalizedCpf));

        if (form == null) {
            log.warn("updateOwnerContact invalid input: form is null. cpf={}", maskCpf(normalizedCpf));
            throw new InvalidInputException("Update form must be provided.");
        }

        if (form.phone() == null && form.email() == null && form.address() == null) {
            log.warn("updateOwnerContact invalid input: no fields. cpf={}", maskCpf(normalizedCpf));
            throw new InvalidInputException("At least one field must be provided: phone, email or address.");
        }

        OwnerEntity owner = ownerRepository.findByCpf(normalizedCpf)
                .orElseThrow(() -> {
                    log.warn("updateOwnerContact failed: owner not found. cpf={}", maskCpf(normalizedCpf));
                    return new EntityNotFoundException("Owner not found.");
                });

        if (owner.getStatus() == Status.INACTIVE) {
            log.warn("updateOwnerContact blocked: inactive owner. ownerId={}", owner.getId());
            throw new DomainRuleException("Inactive owners cannot be updated.");
        }

        String normalizedPhone = form.phone() == null ? null : normalizePhone(form.phone());

        owner.updateContactInfo(normalizedPhone, form.email(), form.address());
        OwnerEntity saved = ownerRepository.save(owner);

        log.info("updateOwnerContact completed: ownerId={}", saved.getId());
        return OwnerMapper.toDTO(saved);
    }

    public OwnerDTO applyAction(String cpf, OwnerAction action) {
        String normalizedCpf = normalizeCpf(cpf);
        log.info("applyOwnerAction started: cpf={}, action={}", maskCpf(normalizedCpf), action);

        if (action == null) {
            log.warn("applyOwnerAction invalid input: action is null. cpf={}", maskCpf(normalizedCpf));
            throw new InvalidInputException("Action must be provided.");
        }

        OwnerEntity owner = ownerRepository.findByCpf(normalizedCpf)
                .orElseThrow(() -> {
                    log.warn("applyOwnerAction failed: owner not found. cpf={}", maskCpf(normalizedCpf));
                    return new EntityNotFoundException("Owner not found.");
                });

        Status before = owner.getStatus();

        if (action == OwnerAction.ACTIVATE) {
            if (owner.getStatus() == Status.ACTIVE) {
                log.warn("applyOwnerAction blocked: already active. ownerId={}", owner.getId());
                throw new DomainRuleException("Owner is already active.");
            }
            owner.activate();
        } else if (action == OwnerAction.DEACTIVATE) {
            if (owner.getStatus() == Status.INACTIVE) {
                log.warn("applyOwnerAction blocked: already inactive. ownerId={}", owner.getId());
                throw new DomainRuleException("Owner is already inactive.");
            }

            List<AppointmentStatus> openStatuses = List.of(
                    AppointmentStatus.SCHEDULED,
                    AppointmentStatus.IN_PROGRESS,
                    AppointmentStatus.WAITING_PAYMENT
            );

            if (appointmentRepository.existsByOwnerIdAndStatusIn(owner.getId(), openStatuses)) {
                log.warn("applyOwnerAction blocked: open appointments exist. ownerId={}", owner.getId());
                throw new DomainRuleException("Owner cannot be inactivated while there are open appointments.");
            }

            owner.deactivate();
        } else {
            log.warn("applyOwnerAction invalid input: unsupported action. ownerId={}, action={}", owner.getId(), action);
            throw new InvalidInputException("Unsupported action.");
        }

        OwnerEntity saved = ownerRepository.save(owner);

        log.info("applyOwnerAction completed: ownerId={}, statusBefore={}, statusAfter={}",
                saved.getId(), before, saved.getStatus());

        return OwnerMapper.toDTO(saved);
    }

    private String normalizeCpf(String cpf) {
        String normalized = CpfUtils.normalize(cpf);
        if (normalized == null) throw new InvalidInputException("CPF must be provided.");
        if (!CpfUtils.hasValidLength(normalized)) throw new InvalidInputException("CPF must contain exactly 11 digits.");
        return normalized;
    }

    private String normalizePhone(String phone) {
        String normalized = PhoneUtils.normalize(phone);
        if (normalized == null || normalized.isBlank()) throw new InvalidInputException("Phone must be provided.");
        if (!PhoneUtils.hasValidLength(normalized)) throw new InvalidInputException("Phone must have 10 or 11 digits (no mask).");
        return normalized;
    }

    private String maskCpf(String cpfDigits) {
        if (cpfDigits == null || cpfDigits.length() != 11) return "***";
        return cpfDigits.substring(0, 3) + ".***.***-" + cpfDigits.substring(9, 11);
    }
}
