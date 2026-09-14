package br.com.dwnl.spicehub.identity.infrastructure.persistence.repository;

import br.com.dwnl.spicehub.identity.domain.model.RoleName;
import br.com.dwnl.spicehub.identity.infrastructure.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface SpringDataRoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(RoleName roleName);
}
