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
        String imageKey,
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
                product.getImageKey(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
