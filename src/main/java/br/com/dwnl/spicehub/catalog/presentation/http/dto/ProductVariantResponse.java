package br.com.dwnl.spicehub.catalog.presentation.http.dto;

import br.com.dwnl.spicehub.catalog.domain.model.MeasurementUnit;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.model.VariantAvailability;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductVariantResponse(
        UUID id,
        UUID productId,
        int quantity,
        MeasurementUnit measurementUnit,
        BigDecimal price,
        VariantAvailability availability,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProductVariantResponse from(ProductVariant productVariant){
        return new ProductVariantResponse(
                productVariant.getId(),
                productVariant.getProductId(),
                productVariant.getQuantity(),
                productVariant.getMeasurementUnit(),
                productVariant.getPrice(),
                productVariant.getAvailability(),
                productVariant.getCreatedAt(),
                productVariant.getUpdatedAt()
        );
    }
}
