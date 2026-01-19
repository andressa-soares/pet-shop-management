package com.br.pet_shop_management.infrastructure.persistence;

import com.br.pet_shop_management.domain.entity.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PetRepository extends JpaRepository<PetEntity, Long>, JpaSpecificationExecutor<PetEntity> {
    @Query("""
    select case when count(p) > 0 then true else false end
    from PetEntity p where p.owner.id = :ownerId and lower(p.name) = lower(:name)""")
    boolean existsPetDuplicate(@Param("ownerId") Long ownerId, @Param("name") String name);
}
