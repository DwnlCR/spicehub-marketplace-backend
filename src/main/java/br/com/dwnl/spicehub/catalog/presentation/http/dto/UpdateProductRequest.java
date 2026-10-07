package br.com.dwnl.spicehub.catalog.presentation.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateProductRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        String description,

        @NotBlank
        UUID categoryId,

        String imageKey
) {
}
