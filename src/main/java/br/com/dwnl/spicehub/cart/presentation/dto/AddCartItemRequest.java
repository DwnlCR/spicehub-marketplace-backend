package br.com.dwnl.spicehub.cart.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddCartItemRequest(
        @NotNull
        UUID variantId,

        @Min(1)
        int quantity
) {
}
