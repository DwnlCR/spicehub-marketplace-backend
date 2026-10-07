package br.com.dwnl.spicehub.catalog.presentation.http.dto;

import br.com.dwnl.spicehub.catalog.domain.model.Category;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(UUID id, String name, boolean active, Instant createdAt, Instant updatedAt) {

    public static CategoryResponse from(Category category){
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
