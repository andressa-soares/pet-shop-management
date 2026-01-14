package com.br.pet_shop_management.application.service;

import com.br.pet_shop_management.api.dto.response.ClientDTO;
import com.br.pet_shop_management.api.dto.request.ClientForm;
import com.br.pet_shop_management.api.dto.request.ClientUpdateForm;
import com.br.pet_shop_management.application.mapper.ClientMapper;
import com.br.pet_shop_management.domain.entity.ClientEntity;
import com.br.pet_shop_management.domain.enums.ClientStatus;
import com.br.pet_shop_management.application.exception.BusinessException;
import com.br.pet_shop_management.infrastructure.persistence.ClientRepository;
import com.br.pet_shop_management.util.CpfUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public List<ClientDTO> findAll() {
        return clientRepository.findByStatus(ClientStatus.ACTIVE)
                .stream()
                .map(ClientMapper::toDTO)
                .toList();
    }

    public ClientDTO findByCpf(String cpf) {
        String normalizedCpf = normalizeCpf(cpf);

        ClientEntity client = clientRepository.findByCpf(normalizedCpf)
                .orElseThrow(() -> new EntityNotFoundException("Client not found."));
        return ClientMapper.toDTO(client);
    }

    public ClientDTO findById(Long id) {
        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found."));
        return ClientMapper.toDTO(client);
    }

    public ClientDTO saveClient(ClientForm clientForm) {
        String normalizedCpf = normalizeCpf(clientForm.cpf());

        if (clientRepository.existsByCpf(normalizedCpf)) {
            throw new BusinessException("Client not found.");
        }

        ClientStatus status = ClientStatus.ACTIVE; // default
        ClientEntity clientEntity = ClientMapper.toEntity(clientForm, normalizedCpf, status);
        ClientEntity saved = clientRepository.save(clientEntity);
        return ClientMapper.toDTO(saved);
    }

    public ClientDTO updateClientContact(String cpf, ClientUpdateForm form) {
        String normalizedCpf = normalizeCpf(cpf);

        if (form.phone() == null && form.email() == null && form.address() == null) {
            throw new BusinessException("At least one field must be provided: phone, email or address.");
        }

        ClientEntity client = clientRepository.findByCpf(normalizedCpf)
                .orElseThrow(() -> new EntityNotFoundException("Client not found."));

        if (client.getStatus() == ClientStatus.INACTIVE) {
            throw new BusinessException("Inactive clients cannot be updated.");
        }

        client.patchContactInfo(form.phone(), form.email(), form.address());

        ClientEntity saved = clientRepository.save(client);
        return ClientMapper.toDTO(saved);
    }

    public ClientDTO activateClient(String cpf) {
        String normalizedCpf = normalizeCpf(cpf);

        ClientEntity client = clientRepository.findByCpf(normalizedCpf)
                .orElseThrow(() -> new EntityNotFoundException("Client not found."));

        if (client.getStatus() == ClientStatus.ACTIVE) {
            throw new BusinessException("Client is already active.");
        }

        client.activate();

        ClientEntity saved = clientRepository.save(client);
        return ClientMapper.toDTO(saved);
    }

    public ClientDTO deleteClient(String cpf) {
        String normalizedCpf = normalizeCpf(cpf);

        ClientEntity client = clientRepository.findByCpf(normalizedCpf)
                .orElseThrow(() -> new EntityNotFoundException("Client not found."));

        if (client.getStatus() == ClientStatus.INACTIVE) {
            throw new BusinessException("Client is already inactive.");
        }

        client.inactivate();

        ClientEntity saved = clientRepository.save(client);
        return ClientMapper.toDTO(saved);
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
}