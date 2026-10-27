package br.com.dwnl.spicehub.cart.presentation.mapper;

import br.com.dwnl.spicehub.cart.application.dto.CartProductSnapshot;
import br.com.dwnl.spicehub.cart.application.port.CartProductQueryPort;
import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.presentation.dto.CartItemResponse;
import br.com.dwnl.spicehub.cart.presentation.dto.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CartResponseMapper {

    private final CartProductQueryPort cartProductQueryPort;

    public CartResponse toResponse(Cart cart){
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> {
                    CartProductSnapshot snapshot = cartProductQueryPort.findByVariantId(item.getVariantId())
                            .orElse(null);

                    BigDecimal unitPrice = snapshot != null ? snapshot.price() : BigDecimal.ZERO;

                    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

                    return new CartItemResponse(
                            item.getId(),
                            snapshot != null ? snapshot.productId() : null,
                            item.getVariantId(),
                            snapshot != null ? snapshot.productName() : null,
                            unitPrice,
                            item.getQuantity(),
                            subtotal,
                            snapshot != null && snapshot.purchasable()
                    );
                })
                .toList();

        int totalItems = items.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();

        BigDecimal totalPrice = items.stream()
                .filter(CartItemResponse::available)
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                items,
                totalItems,
                totalPrice
        );
    }
}
