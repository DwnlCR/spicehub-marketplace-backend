package br.com.dwnl.spicehub.catalog.presentation.http.dto;

import br.com.dwnl.spicehub.catalog.domain.model.MeasurementUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateProductVariantRequest(

        @Positive
        int quantity,

        @NotNull
        MeasurementUnit measurementUnit,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal price
) {
}
