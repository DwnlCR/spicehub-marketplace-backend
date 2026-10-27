package br.com.dwnl.spicehub.cart.presentation.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productId,
        UUID variantId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal,
        boolean available
) {
}
