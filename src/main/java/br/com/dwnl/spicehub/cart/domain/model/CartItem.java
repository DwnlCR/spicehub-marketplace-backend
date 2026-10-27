package br.com.dwnl.spicehub.cart.domain.model;

import br.com.dwnl.spicehub.cart.domain.exception.InvalidCartItemQuantityException;
import lombok.Getter;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Getter
public class CartItem {

    private final UUID id;
    private final UUID variantId;
    private int quantity;

    public CartItem(UUID id, UUID variantId, int quantity) {
        this.id = requireNonNull(id, "Cart item id cannot be null");
        this.variantId = requireNonNull(variantId, "Variant id cannot be null");
        this.quantity = validateQuantity(quantity);
    }

    public static CartItem create(UUID variantId, int quantity){
        return new CartItem(UUID.randomUUID(), variantId, quantity);
    }

    public static CartItem restore(UUID id, UUID variantId, int quantity){
        return new CartItem(id, variantId, quantity);
    }

    void changeQuantity(int quantity){
        this.quantity = validateQuantity(quantity);
    }

    private static int validateQuantity(int quantity){
        if (quantity <= 0){
            throw new InvalidCartItemQuantityException(
                    "Cart item quantity must be greater than zero"
            );
        }

        return quantity;
    }
}
