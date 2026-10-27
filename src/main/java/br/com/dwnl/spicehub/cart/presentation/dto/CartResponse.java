package br.com.dwnl.spicehub.cart.presentation.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        List<CartItemResponse> items,
        int totalItems,
        BigDecimal totalPrice
) {
}
