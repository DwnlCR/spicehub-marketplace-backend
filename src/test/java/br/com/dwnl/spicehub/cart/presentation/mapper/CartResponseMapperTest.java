
package br.com.dwnl.spicehub.cart.presentation.mapper;

import br.com.dwnl.spicehub.cart.application.dto.CartProductSnapshot;
import br.com.dwnl.spicehub.cart.application.port.CartProductQueryPort;
import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.presentation.dto.CartItemResponse;
import br.com.dwnl.spicehub.cart.presentation.dto.CartResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartResponseMapperTest {

    @Mock
    private CartProductQueryPort cartProductQueryPort;

    private CartResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CartResponseMapper(cartProductQueryPort);
    }

    @Test
    void shouldReturnEmptyCart() {
        Cart cart = Cart.create(UUID.randomUUID());

        CartResponse response = mapper.toResponse(cart);

        assertEquals(cart.getId(), response.id());
        assertTrue(response.items().isEmpty());
        assertEquals(0, response.totalItems());
        assertEquals(0, response.totalPrice().compareTo(BigDecimal.ZERO));

        verifyNoInteractions(cartProductQueryPort);
    }

    @Test
    void shouldCalculateItemSubtotalAndCartTotal() {
        Cart cart = Cart.create(UUID.randomUUID());
        UUID productId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();

        cart.addItem(variantId, 3);

        CartProductSnapshot snapshot = new CartProductSnapshot(
                productId,
                variantId,
                "Chá de Camomila",
                new BigDecimal("15.90"),
                true
        );

        when(cartProductQueryPort.findByVariantId(variantId))
                .thenReturn(Optional.of(snapshot));

        CartResponse response = mapper.toResponse(cart);
        CartItemResponse item = response.items().getFirst();

        assertEquals(1, response.items().size());
        assertEquals(productId, item.productId());
        assertEquals(variantId, item.variantId());
        assertEquals("Chá de Camomila", item.productName());
        assertEquals(3, item.quantity());
        assertEquals(0, item.unitPrice().compareTo(new BigDecimal("15.90")));
        assertEquals(0, item.subtotal().compareTo(new BigDecimal("47.70")));
        assertTrue(item.available());

        assertEquals(3, response.totalItems());
        assertEquals(0, response.totalPrice().compareTo(new BigDecimal("47.70")));
    }

    @Test
    void shouldSumMultipleAvailableItems() {
        Cart cart = Cart.create(UUID.randomUUID());

        UUID firstVariantId = UUID.randomUUID();
        UUID secondVariantId = UUID.randomUUID();

        cart.addItem(firstVariantId, 2);
        cart.addItem(secondVariantId, 3);

        when(cartProductQueryPort.findByVariantId(firstVariantId))
                .thenReturn(Optional.of(new CartProductSnapshot(
                        UUID.randomUUID(),
                        firstVariantId,
                        "Camomila",
                        new BigDecimal("10.00"),
                        true
                )));

        when(cartProductQueryPort.findByVariantId(secondVariantId))
                .thenReturn(Optional.of(new CartProductSnapshot(
                        UUID.randomUUID(),
                        secondVariantId,
                        "Hortelã",
                        new BigDecimal("5.50"),
                        true
                )));

        CartResponse response = mapper.toResponse(cart);

        assertEquals(2, response.items().size());
        assertEquals(5, response.totalItems());
        assertEquals(0, response.totalPrice().compareTo(new BigDecimal("36.50")));
    }

    @Test
    void shouldExcludeUnavailableItemsFromTotalPrice() {
        Cart cart = Cart.create(UUID.randomUUID());

        UUID availableVariantId = UUID.randomUUID();
        UUID unavailableVariantId = UUID.randomUUID();

        cart.addItem(availableVariantId, 2);
        cart.addItem(unavailableVariantId, 3);

        when(cartProductQueryPort.findByVariantId(availableVariantId))
                .thenReturn(Optional.of(new CartProductSnapshot(
                        UUID.randomUUID(),
                        availableVariantId,
                        "Camomila",
                        new BigDecimal("10.00"),
                        true
                )));

        when(cartProductQueryPort.findByVariantId(unavailableVariantId))
                .thenReturn(Optional.of(new CartProductSnapshot(
                        UUID.randomUUID(),
                        unavailableVariantId,
                        "Hortelã",
                        new BigDecimal("8.00"),
                        false
                )));

        CartResponse response = mapper.toResponse(cart);

        assertEquals(5, response.totalItems());
        assertEquals(0, response.totalPrice().compareTo(new BigDecimal("20.00")));

        CartItemResponse unavailableItem = response.items().stream()
                .filter(item -> item.variantId().equals(unavailableVariantId))
                .findFirst()
                .orElseThrow();

        assertFalse(unavailableItem.available());
        assertEquals(0, unavailableItem.subtotal().compareTo(new BigDecimal("24.00")));
    }

    @Test
    void shouldHandleMissingVariant() {
        Cart cart = Cart.create(UUID.randomUUID());
        UUID variantId = UUID.randomUUID();

        cart.addItem(variantId, 2);

        when(cartProductQueryPort.findByVariantId(variantId))
                .thenReturn(Optional.empty());

        CartResponse response = mapper.toResponse(cart);
        CartItemResponse item = response.items().getFirst();

        assertNull(item.productId());
        assertNull(item.productName());
        assertFalse(item.available());
        assertEquals(2, item.quantity());
        assertEquals(0, item.unitPrice().compareTo(BigDecimal.ZERO));
        assertEquals(0, item.subtotal().compareTo(BigDecimal.ZERO));
        assertEquals(0, response.totalPrice().compareTo(BigDecimal.ZERO));
    }
}
