package com.br.pet_shop_management.infrastructure.persistence;

import com.br.pet_shop_management.domain.entity.ClientEntity;
import com.br.pet_shop_management.domain.enums.ClientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {
    boolean existsByCpf(String cpf);
    Optional<ClientEntity> findByCpf(String cpf);
    List<ClientEntity> findByStatus(ClientStatus status);

}