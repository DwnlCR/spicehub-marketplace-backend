package br.com.dwnl.spicehub.catalog.presentation.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateProductRequest(

        @NotBlank
        @Size
        String name,

        String description,

        @NotNull
        UUID categoryId,

        String imageKey
) {
}
