package br.com.dwnl.spicehub.catalog.infrastructure.persistence.repository;

import br.com.dwnl.spicehub.catalog.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<ProductEntity, UUID> {

    List<ProductEntity> findByCategoryId(UUID categoryId);

    boolean existsByNameIgnoreCase(String name);

    Page<ProductEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<ProductEntity> findByCategoryId(UUID categoryId, Pageable pageable);

    Page<ProductEntity> findByCategoryIdAndNameContainingIgnoreCase(UUID categoryId, String name, Pageable pageable);
}
