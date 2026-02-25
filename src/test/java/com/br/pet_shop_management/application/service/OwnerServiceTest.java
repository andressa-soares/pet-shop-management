package com.br.pet_shop_management.application.service;

import com.br.pet_shop_management.api.dto.request.OwnerForm;
import com.br.pet_shop_management.api.dto.request.OwnerUpdateForm;
import com.br.pet_shop_management.api.dto.request.enums.OwnerAction;
import com.br.pet_shop_management.api.dto.response.OwnerDTO;
import com.br.pet_shop_management.application.exception.DomainRuleException;
import com.br.pet_shop_management.application.exception.InvalidInputException;
import com.br.pet_shop_management.domain.entity.OwnerEntity;
import com.br.pet_shop_management.domain.enums.Status;
import com.br.pet_shop_management.infrastructure.persistence.AppointmentRepository;
import com.br.pet_shop_management.infrastructure.persistence.OwnerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

    @Mock
    OwnerRepository ownerRepository;

    @Mock
    AppointmentRepository appointmentRepository;

    @InjectMocks
    OwnerService ownerService;

    private static OwnerForm validOwnerForm(String cpf, String phone) {
        return new OwnerForm(
                "João da Silva",
                cpf,
                phone,
                "joao@email.com",
                "Rua X, 123"
        );
    }

    private static OwnerEntity ownerActive(Long id, String cpfDigits, String phoneDigits) {
        OwnerEntity e = new OwnerEntity(
                "João da Silva",
                cpfDigits,
                phoneDigits,
                "joao@email.com",
                "Rua X, 123",
                Status.ACTIVE);

        return e;
    }

    // ---------- findAll ----------

    @Test
    void findAll_shouldReturnActiveOwnersPage() {
        Pageable pageable = PageRequest.of(0, 10);
        OwnerEntity entity = ownerActive(1L, "12345678901", "11999998888");

        when(ownerRepository.findByStatus(eq(Status.ACTIVE), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

        Page<OwnerDTO> result = ownerService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(ownerRepository).findByStatus(Status.ACTIVE, pageable);
        verifyNoInteractions(appointmentRepository);
    }

    // ---------- findByCpf ----------

    @Test
    void findByCpf_shouldThrowWhenCpfNull() {
        assertThatThrownBy(() -> ownerService.findByCpf(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("CPF must be provided.");

        verifyNoInteractions(ownerRepository, appointmentRepository);
    }

    @Test
    void findByCpf_shouldThrowWhenCpfInvalidLength() {
        assertThatThrownBy(() -> ownerService.findByCpf("123"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("CPF must contain exactly 11 digits.");

        verifyNoInteractions(ownerRepository, appointmentRepository);
    }

    @Test
    void findByCpf_shouldReturnDtoWhenFound() {
        String cpfMasked = "123.456.789-01"; // normalize -> 12345678901
        OwnerEntity entity = ownerActive(1L, "12345678901", "11999998888");

        when(ownerRepository.findByCpf("12345678901")).thenReturn(Optional.of(entity));

        OwnerDTO dto = ownerService.findByCpf(cpfMasked);

        assertThat(dto).isNotNull();
        assertThat(dto.status()).isEqualTo(Status.ACTIVE);

        verify(ownerRepository).findByCpf("12345678901");
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void findByCpf_shouldThrowWhenNotFound() {
        when(ownerRepository.findByCpf("12345678901")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.findByCpf("123.456.789-01"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Owner not found.");

        verify(ownerRepository).findByCpf("12345678901");
        verifyNoInteractions(appointmentRepository);
    }

    // ---------- findById ----------

    @Test
    void findById_shouldReturnDtoWhenFound() {
        OwnerEntity entity = ownerActive(1L, "12345678901", "11999998888");
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(entity));

        OwnerDTO dto = ownerService.findById(1L);

        assertThat(dto).isNotNull();
        verify(ownerRepository).findById(1L);
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(ownerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Owner not found.");

        verify(ownerRepository).findById(99L);
        verifyNoInteractions(appointmentRepository);
    }

    // ---------- saveOwner ----------

    @Test
    void saveOwner_shouldThrowWhenFormNull() {
        assertThatThrownBy(() -> ownerService.saveOwner(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Owner form must be provided.");

        verifyNoInteractions(ownerRepository, appointmentRepository);
    }

    @Test
    void saveOwner_shouldThrowWhenCpfInvalid() {
        OwnerForm form = validOwnerForm("123", "11999998888");

        assertThatThrownBy(() -> ownerService.saveOwner(form))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("CPF must contain exactly 11 digits.");

        verifyNoInteractions(ownerRepository, appointmentRepository);
    }

    @Test
    void saveOwner_shouldThrowWhenPhoneInvalid() {
        OwnerForm form = validOwnerForm("12345678901", "123"); // inválido

        assertThatThrownBy(() -> ownerService.saveOwner(form))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Phone must have 10 or 11 digits (no mask).");

        verifyNoInteractions(ownerRepository, appointmentRepository);
    }

    @Test
    void saveOwner_shouldThrowWhenCpfAlreadyExists() {
        OwnerForm form = validOwnerForm("123.456.789-01", "11999998888");

        when(ownerRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> ownerService.saveOwner(form))
                .isInstanceOf(DomainRuleException.class)
                .hasMessage("CPF already exists.");

        verify(ownerRepository).existsByCpf("12345678901");
        verify(ownerRepository, never()).save(any());
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void saveOwner_shouldSaveWhenValid() {
        OwnerForm form = validOwnerForm("123.456.789-01", "11999998888");
        OwnerEntity saved = ownerActive(1L, "12345678901", "11999998888");

        when(ownerRepository.existsByCpf("12345678901")).thenReturn(false);
        when(ownerRepository.save(any(OwnerEntity.class))).thenReturn(saved);

        OwnerDTO dto = ownerService.saveOwner(form);

        assertThat(dto).isNotNull();
        assertThat(dto.status()).isEqualTo(Status.ACTIVE);

        verify(ownerRepository).existsByCpf("12345678901");
        verify(ownerRepository).save(any(OwnerEntity.class));
        verifyNoInteractions(appointmentRepository);
    }

    // ---------- updateOwnerContact ----------

    @Test
    void updateOwnerContact_shouldThrowWhenFormNull() {
        assertThatThrownBy(() -> ownerService.updateOwnerContact("12345678901", null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Update form must be provided.");

        verifyNoInteractions(ownerRepository, appointmentRepository);
    }

    @Test
    void updateOwnerContact_shouldThrowWhenNoFieldsProvided() {
        OwnerUpdateForm form = new OwnerUpdateForm(null, null, null);

        assertThatThrownBy(() -> ownerService.updateOwnerContact("12345678901", form))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("At least one field must be provided: phone, email or address.");

        verifyNoInteractions(ownerRepository, appointmentRepository);
    }

    @Test
    void updateOwnerContact_shouldThrowWhenOwnerNotFound() {
        OwnerUpdateForm form = new OwnerUpdateForm("(11) 99999-9999", null, null);

        when(ownerRepository.findByCpf("12345678901")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.updateOwnerContact("123.456.789-01", form))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Owner not found.");

        verify(ownerRepository).findByCpf("12345678901");
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void updateOwnerContact_shouldThrowWhenOwnerInactive() {
        OwnerUpdateForm form = new OwnerUpdateForm("(11) 99999-9999", null, null);

        OwnerEntity owner = ownerActive(1L, "12345678901", "11999998888");
        owner.deactivate();

        when(ownerRepository.findByCpf("12345678901")).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> ownerService.updateOwnerContact("12345678901", form))
                .isInstanceOf(DomainRuleException.class)
                .hasMessage("Inactive owners cannot be updated.");

        verify(ownerRepository).findByCpf("12345678901");
        verify(ownerRepository, never()).save(any());
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void updateOwnerContact_shouldSaveWhenValid() {
        OwnerUpdateForm form = new OwnerUpdateForm("(11) 99999-9999", "novo@email.com", "Rua Y, 456");

        OwnerEntity owner = ownerActive(1L, "12345678901", "11999998888");

        when(ownerRepository.findByCpf("12345678901")).thenReturn(Optional.of(owner));
        when(ownerRepository.save(any(OwnerEntity.class))).thenReturn(owner);

        OwnerDTO dto = ownerService.updateOwnerContact("123.456.789-01", form);

        assertThat(dto).isNotNull();

        verify(ownerRepository).findByCpf("12345678901");
        verify(ownerRepository).save(owner);
        verifyNoInteractions(appointmentRepository);
    }

    // ---------- applyAction ----------

    @Test
    void applyAction_shouldThrowWhenActionNull() {
        assertThatThrownBy(() -> ownerService.applyAction("12345678901", null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Action must be provided.");

        verifyNoInteractions(ownerRepository, appointmentRepository);
    }

    @Test
    void applyAction_shouldThrowWhenOwnerNotFound() {
        when(ownerRepository.findByCpf("12345678901")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.applyAction("123.456.789-01", OwnerAction.ACTIVATE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Owner not found.");

        verify(ownerRepository).findByCpf("12345678901");
        verifyNoMoreInteractions(ownerRepository);
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void applyAction_activate_shouldThrowWhenAlreadyActive() {
        OwnerEntity owner = ownerActive(1L, "12345678901", "11999998888");

        when(ownerRepository.findByCpf("12345678901")).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> ownerService.applyAction("12345678901", OwnerAction.ACTIVATE))
                .isInstanceOf(DomainRuleException.class)
                .hasMessage("Owner is already active.");

        verify(ownerRepository).findByCpf("12345678901");
        verify(ownerRepository, never()).save(any());
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void applyAction_activate_shouldSaveWhenInactive() {
        OwnerEntity owner = ownerActive(1L, "12345678901", "11999998888");
        owner.deactivate();

        when(ownerRepository.findByCpf("12345678901")).thenReturn(Optional.of(owner));
        when(ownerRepository.save(any(OwnerEntity.class))).thenReturn(owner);

        OwnerDTO dto = ownerService.applyAction("12345678901", OwnerAction.ACTIVATE);

        assertThat(dto).isNotNull();
        assertThat(dto.status()).isEqualTo(Status.ACTIVE);

        verify(ownerRepository).findByCpf("12345678901");
        verify(ownerRepository).save(owner);
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void applyAction_deactivate_shouldThrowWhenAlreadyInactive() {
        OwnerEntity owner = ownerActive(1L, "12345678901", "11999998888");
        owner.deactivate();

        when(ownerRepository.findByCpf("12345678901")).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> ownerService.applyAction("12345678901", OwnerAction.DEACTIVATE))
                .isInstanceOf(DomainRuleException.class)
                .hasMessage("Owner is already inactive.");

        verify(ownerRepository).findByCpf("12345678901");
        verify(ownerRepository, never()).save(any());
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void applyAction_deactivate_shouldThrowWhenOpenAppointmentsExist() {
        OwnerEntity owner = ownerActive(1L, "12345678901", "11999998888");

        when(ownerRepository.findByCpf("12345678901")).thenReturn(Optional.of(owner));
        when(appointmentRepository.existsByOwnerIdAndStatusIn(eq(owner.getId()), anyList()))
                .thenReturn(true);

        assertThatThrownBy(() -> ownerService.applyAction("12345678901", OwnerAction.DEACTIVATE))
                .isInstanceOf(DomainRuleException.class)
                .hasMessage("Owner cannot be inactivated while there are open appointments.");

        verify(ownerRepository).findByCpf("12345678901");
        verify(appointmentRepository).existsByOwnerIdAndStatusIn(eq(owner.getId()), anyList());
        verify(ownerRepository, never()).save(any());
    }

    @Test
    void applyAction_deactivate_shouldSaveWhenNoOpenAppointments() {
        OwnerEntity owner = ownerActive(1L, "12345678901", "11999998888");

        when(ownerRepository.findByCpf("12345678901")).thenReturn(Optional.of(owner));
        when(appointmentRepository.existsByOwnerIdAndStatusIn(eq(owner.getId()), anyList()))
                .thenReturn(false);
        when(ownerRepository.save(any(OwnerEntity.class))).thenReturn(owner);

        OwnerDTO dto = ownerService.applyAction("12345678901", OwnerAction.DEACTIVATE);

        assertThat(dto).isNotNull();
        assertThat(dto.status()).isEqualTo(Status.INACTIVE);

        verify(ownerRepository).findByCpf("12345678901");
        verify(appointmentRepository).existsByOwnerIdAndStatusIn(eq(owner.getId()), anyList());
        verify(ownerRepository).save(owner);
    }
}