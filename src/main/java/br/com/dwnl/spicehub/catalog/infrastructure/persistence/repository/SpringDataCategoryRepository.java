package br.com.dwnl.spicehub.catalog.infrastructure.persistence.repository;

import br.com.dwnl.spicehub.catalog.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataCategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    boolean existsByNameIgnoreCase(String name);
}
