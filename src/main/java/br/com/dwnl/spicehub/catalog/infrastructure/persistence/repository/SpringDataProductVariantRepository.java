package br.com.dwnl.spicehub.catalog.infrastructure.persistence.repository;

import br.com.dwnl.spicehub.catalog.domain.model.MeasurementUnit;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.entity.ProductVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataProductVariantRepository extends JpaRepository<ProductVariantEntity, UUID> {

    List<ProductVariantEntity> findByProductId(UUID productId);

    boolean existsByProductIdAndQuantityAndMeasurementUnit(UUID productId, int quantity, MeasurementUnit measurementUnit);

    boolean existsByProductIdAndQuantityAndMeasurementUnitAndIdNot(UUID productId, int quantity, MeasurementUnit measurementUnit, UUID id);
}
