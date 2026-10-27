package br.com.dwnl.spicehub.cart.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public record CartProductSnapshot(
        UUID productId,
        UUID variantId,
        String productName,
        BigDecimal price,
        boolean purchasable
) {

    public CartProductSnapshot {
        requireNonNull(productId, "Product id cannot be null");
        requireNonNull(variantId, "Variant id cannot be null");
        requireNonNull(productName, "Product name cannot be null");
        requireNonNull(price, "Variant price cannot be null");
    }
}
