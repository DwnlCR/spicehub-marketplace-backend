package br.com.dwnl.spicehub.cart.infrastructure.persistence.repository;

import br.com.dwnl.spicehub.cart.infrastructure.persistence.entity.CartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataCartRepository extends JpaRepository<CartJpaEntity, UUID> {

    Optional<CartJpaEntity> findByUserId(UUID userId);
}
