package br.com.dwnl.spicehub.identity.infrastructure.persistence.repository;

import br.com.dwnl.spicehub.identity.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findWithRolesById(UUID id);
}
