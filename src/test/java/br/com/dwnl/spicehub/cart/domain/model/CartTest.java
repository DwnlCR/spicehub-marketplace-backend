
package br.com.dwnl.spicehub.cart.domain.model;

import br.com.dwnl.spicehub.cart.domain.exception.CartItemNotFoundException;
import br.com.dwnl.spicehub.cart.domain.exception.InvalidCartItemQuantityException;
import br.com.dwnl.spicehub.cart.domain.exception.InvalidCartItemsException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CartTest {

    @Test
    void shouldCreateEmptyCart() {
        UUID userId = UUID.randomUUID();

        Cart cart = Cart.create(userId);

        assertNotNull(cart.getId());
        assertEquals(userId, cart.getUserId());
        assertTrue(cart.getItems().isEmpty());
        assertNotNull(cart.getCreatedAt());
        assertEquals(cart.getCreatedAt(), cart.getUpdatedAt());
    }

    @Test
    void shouldAddItem() {
        Cart cart = Cart.create(UUID.randomUUID());
        UUID variantId = UUID.randomUUID();

        cart.addItem(variantId, 2);

        assertEquals(1, cart.getItems().size());
        assertEquals(variantId, cart.getItems().getFirst().getVariantId());
        assertEquals(2, cart.getItems().getFirst().getQuantity());
    }

    @Test
    void shouldAccumulateQuantityForExistingVariant() {
        Cart cart = Cart.create(UUID.randomUUID());
        UUID variantId = UUID.randomUUID();

        cart.addItem(variantId, 2);
        cart.addItem(variantId, 3);

        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().getFirst().getQuantity());
    }

    @Test
    void shouldChangeItemQuantity() {
        Cart cart = Cart.create(UUID.randomUUID());
        UUID variantId = UUID.randomUUID();

        cart.addItem(variantId, 2);
        cart.changeItemQuantity(variantId, 5);

        assertEquals(5, cart.getItems().getFirst().getQuantity());
    }

    @Test
    void shouldRemoveItem() {
        Cart cart = Cart.create(UUID.randomUUID());
        UUID variantId = UUID.randomUUID();

        cart.addItem(variantId, 2);
        cart.removeItem(variantId);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void shouldClearCart() {
        Cart cart = Cart.create(UUID.randomUUID());

        cart.addItem(UUID.randomUUID(), 2);
        cart.addItem(UUID.randomUUID(), 3);

        cart.clear();

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void shouldRejectInvalidQuantity() {
        Cart cart = Cart.create(UUID.randomUUID());

        assertThrows(
                InvalidCartItemQuantityException.class,
                () -> cart.addItem(UUID.randomUUID(), 0)
        );
    }

    @Test
    void shouldRejectChangingQuantityToNegativeValue() {
        Cart cart = Cart.create(UUID.randomUUID());
        UUID variantId = UUID.randomUUID();

        cart.addItem(variantId, 2);

        assertThrows(
                InvalidCartItemQuantityException.class,
                () -> cart.changeItemQuantity(variantId, -1)
        );

        assertEquals(2, cart.getItems().getFirst().getQuantity());
    }

    @Test
    void shouldRejectRemovingNonexistentItem() {
        Cart cart = Cart.create(UUID.randomUUID());

        assertThrows(
                CartItemNotFoundException.class,
                () -> cart.removeItem(UUID.randomUUID())
        );
    }

    @Test
    void shouldRejectDuplicateVariantsOnRestore() {
        UUID variantId = UUID.randomUUID();

        List<CartItem> items = List.of(
                CartItem.create(variantId, 2),
                CartItem.create(variantId, 3)
        );

        Instant now = Instant.now();

        assertThrows(
                InvalidCartItemsException.class,
                () -> Cart.restore(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        items,
                        now,
                        now
                )
        );
    }

    @Test
    void shouldRestoreCart() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now();

        Cart cart = Cart.restore(
                id,
                userId,
                List.of(CartItem.create(variantId, 2)),
                createdAt,
                updatedAt
        );

        assertEquals(id, cart.getId());
        assertEquals(userId, cart.getUserId());
        assertEquals(1, cart.getItems().size());
        assertEquals(createdAt, cart.getCreatedAt());
        assertEquals(updatedAt, cart.getUpdatedAt());
    }

    @Test
    void shouldNotExposeMutableItemsList() {
        Cart cart = Cart.create(UUID.randomUUID());

        cart.addItem(UUID.randomUUID(), 2);

        assertThrows(
                UnsupportedOperationException.class,
                () -> cart.getItems().clear()
        );

        assertEquals(1, cart.getItems().size());
    }
}
