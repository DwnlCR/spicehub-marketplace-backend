package br.com.dwnl.spicehub.catalog.presentation.http.dto;

import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductStatus;

import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        UUID categoryId,
        boolean hasImage,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProductResponse from(Product product){
        return new ProductResponse(
          product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategoryId(),
                product.getImageKey() != null,
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
