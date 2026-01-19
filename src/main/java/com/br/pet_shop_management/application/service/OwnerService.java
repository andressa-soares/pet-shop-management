package com.br.pet_shop_management.application.service;

import com.br.pet_shop_management.api.dto.response.OwnerDTO;
import com.br.pet_shop_management.api.dto.request.OwnerForm;
import com.br.pet_shop_management.api.dto.request.OwnerUpdateForm;
import com.br.pet_shop_management.application.mapper.OwnerMapper;
import com.br.pet_shop_management.domain.entity.OwnerEntity;
import com.br.pet_shop_management.domain.enums.AppointmentStatus;
import com.br.pet_shop_management.domain.enums.Status;
import com.br.pet_shop_management.application.exception.BusinessException;
import com.br.pet_shop_management.infrastructure.persistence.AppointmentRepository;
import com.br.pet_shop_management.infrastructure.persistence.OwnerRepository;
import com.br.pet_shop_management.util.CpfUtils;
import com.br.pet_shop_management.util.PhoneUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final AppointmentRepository appointmentRepository;

    public Page<OwnerDTO> findAll(Pageable pageable) {
        return ownerRepository.findByStatus(Status.ACTIVE, pageable)
                .map(OwnerMapper::toDTO);
    }

    public OwnerDTO findByCpf(String cpf) {
        String normalizedCpf = normalizeCpf(cpf);

        OwnerEntity owner = ownerRepository.findByCpf(normalizedCpf)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found."));
        return OwnerMapper.toDTO(owner);
    }

    public OwnerDTO findById(Long id) {
        OwnerEntity owner = ownerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found."));
        return OwnerMapper.toDTO(owner);
    }

    public OwnerDTO saveOwner(OwnerForm ownerForm) {
        String normalizedCpf = normalizeCpf(ownerForm.cpf());
        String normalizedPhone = normalizePhone(ownerForm.phone());

        if (ownerRepository.existsByCpf(normalizedCpf)) {
            throw new BusinessException("CPF already exists.");
        }

        Status status = Status.ACTIVE;

        OwnerEntity ownerEntity = OwnerMapper.toEntity(ownerForm, normalizedCpf, normalizedPhone, status);

        OwnerEntity saved = ownerRepository.save(ownerEntity);
        return OwnerMapper.toDTO(saved);
    }

    public OwnerDTO updateOwnerContact(String cpf, OwnerUpdateForm form) {
        String normalizedCpf = normalizeCpf(cpf);

        if (form.phone() == null && form.email() == null && form.address() == null) {
            throw new BusinessException("At least one field must be provided: phone, email or address.");
        }

        OwnerEntity owner = ownerRepository.findByCpf(normalizedCpf)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found."));

        if (owner.getStatus() == Status.INACTIVE) {
            throw new BusinessException("Inactive owners cannot be updated.");
        }

        String normalizedPhone = form.phone() == null ? null : normalizePhone(form.phone());

        owner.updateContactInfo(normalizedPhone, form.email(), form.address());

        OwnerEntity saved = ownerRepository.save(owner);
        return OwnerMapper.toDTO(saved);
    }

    public OwnerDTO activateOwner(String cpf) {
        String normalizedCpf = normalizeCpf(cpf);

        OwnerEntity owner = ownerRepository.findByCpf(normalizedCpf)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found."));

        if (owner.getStatus() == Status.ACTIVE) {
            throw new BusinessException("Owner is already active.");
        }

        owner.activate();

        OwnerEntity saved = ownerRepository.save(owner);
        return OwnerMapper.toDTO(saved);
    }

    public OwnerDTO deactivateOwner(String cpf) {
        String normalizedCpf = normalizeCpf(cpf);

        OwnerEntity owner = ownerRepository.findByCpf(normalizedCpf)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found."));

        if (owner.getStatus() == Status.INACTIVE) {
            throw new BusinessException("Owner is already inactive.");
        }

        List<AppointmentStatus> openStatuses = List.of(AppointmentStatus.SCHEDULED,
                                                       AppointmentStatus.IN_PROGRESS,
                                                       AppointmentStatus.WAITING_PAYMENT);

        if (appointmentRepository.existsByOwnerIdAndStatusIn(owner.getId(), openStatuses)) {
            throw new BusinessException("Owner cannot be inactivated while there are open appointments.");
        }

        owner.deactivate();

        OwnerEntity saved = ownerRepository.save(owner);
        return OwnerMapper.toDTO(saved);
    }

    private String normalizeCpf(String cpf) {
        String normalized = CpfUtils.normalize(cpf);

        if (normalized == null) {
            throw new BusinessException("CPF must be provided.");
        }

        if (!CpfUtils.hasValidLength(normalized)) {
            throw new BusinessException("CPF must contain exactly 11 digits.");
        }
        return normalized;
    }

    private String normalizePhone(String phone) {
        String normalized = PhoneUtils.normalize(phone);

        if (normalized == null || normalized.isBlank()) {
            throw new BusinessException("Phone must be provided.");
        }

        if (!PhoneUtils.hasValidLength(normalized)) {
            throw new BusinessException("Phone must have 10 or 11 digits (no mask).");
        }

        return normalized;
    }
}