package br.com.dwnl.spicehub.catalog.domain.repository;

import br.com.dwnl.spicehub.catalog.domain.model.MeasurementUnit;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVariantRepository {

    ProductVariant save(ProductVariant productVariant);

    Optional<ProductVariant> findById(UUID id);

    List<ProductVariant> findByProductId(UUID productId);

    boolean existsByProductIdAndQuantityAndMeasurementUnit(UUID productId, int quantity, MeasurementUnit measurementUnit);

    boolean existsByProductIdAndQuantityAndMeasurementUnitAndIdNot(UUID productId, int quantity, MeasurementUnit measurementUnit, UUID id);
}
