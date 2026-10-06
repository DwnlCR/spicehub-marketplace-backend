package br.com.dwnl.spicehub.catalog.presentation.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameCategoryRequest(

        @NotBlank
        @Size(max = 100)
        String name
) {
}
