package br.com.dwnl.spicehub.cart.domain.model;

import br.com.dwnl.spicehub.cart.domain.exception.CartItemNotFoundException;
import br.com.dwnl.spicehub.cart.domain.exception.InvalidCartItemQuantityException;
import br.com.dwnl.spicehub.cart.domain.exception.InvalidCartItemsException;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Getter
public class Cart {

    private final UUID id;
    private final UUID userId;
    private final List<CartItem> items;
    private final Instant createdAt;
    private Instant updatedAt;

    private Cart(UUID id, UUID userId, List<CartItem> items, Instant createdAt, Instant updatedAt) {
        this.id = requireNonNull(id, "Cart id cannot be null");
        this.userId = requireNonNull(userId, "User id cannot be null");
        this.items = new ArrayList<>(requireNonNull(items, "Cart items cannot be null"));
        this.createdAt = requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = requireNonNull(updatedAt, "UpdatedAt cannot be null");

        if (this.items.stream().anyMatch((item -> item == null))) {
            throw new InvalidCartItemsException("Cart items cannot contain null");
        }

        long distinctVariants = this.items.stream()
                .map(CartItem::getVariantId)
                .distinct()
                .count();

        if (distinctVariants != this.items.size()) {
            throw new InvalidCartItemsException("Cart cannot contain duplicate variants");
        }
    }

    public static Cart create(UUID userId) {
        Instant now = Instant.now();

        return new Cart(UUID.randomUUID(), userId, List.of(), now, now);
    }

    public static Cart restore(UUID id, UUID userId, List<CartItem> items, Instant createdAt, Instant updatedAt){
        return new Cart(id, userId, items, createdAt, updatedAt);
    }

    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    public void addItem(UUID variantId, int quantity) {
        validateQuantity(quantity);

        CartItem existingItem = findItem(variantId);

        if (existingItem != null){
            existingItem.changeQuantity(Math.addExact(existingItem.getQuantity(), quantity));
        }
        else {
            items.add(CartItem.create(variantId, quantity));
        }

        touch();
    }

    public void changeItemQuantity(UUID variantId, int quantity) {
        validateQuantity(quantity);

        CartItem item = requireItem(variantId);

        if (item.getQuantity() != quantity) {
            item.changeQuantity(quantity);
            touch();
        }
    }

    public void removeItem(UUID variantId){
        CartItem item = requireItem(variantId);

        items.remove(item);
        touch();
    }

    public void clear(){
        if (!items.isEmpty()){
            items.clear();
            touch();
        }
    }

    private CartItem findItem(UUID variantId) {
        requireNonNull(variantId, "Variat id cannot be null");

        return items.stream()
                .filter(item -> item.getVariantId().equals(variantId))
                .findFirst()
                .orElse(null);
    }

    private CartItem requireItem(UUID variantId){
        CartItem item = findItem(variantId);

        if (item == null){
            throw new CartItemNotFoundException("Cart item not found");
        }

        return item;
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidCartItemQuantityException(
                    "Cart item quantity must be greater than zero"
            );
        }
    }

    private void touch(){
        updatedAt = Instant.now();
    }
}