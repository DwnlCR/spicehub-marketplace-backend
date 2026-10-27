
package br.com.dwnl.spicehub.cart.application.usecase;

import br.com.dwnl.spicehub.cart.application.dto.CartProductSnapshot;
import br.com.dwnl.spicehub.cart.application.exception.CartVariantNotFoundException;
import br.com.dwnl.spicehub.cart.application.exception.CartVariantUnavailableException;
import br.com.dwnl.spicehub.cart.application.port.CartProductQueryPort;
import br.com.dwnl.spicehub.cart.domain.exception.CartItemNotFoundException;
import br.com.dwnl.spicehub.cart.application.exception.CartNotFoundException;
import br.com.dwnl.spicehub.cart.domain.exception.InvalidCartItemQuantityException;
import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.domain.repository.CartRepository;
import br.com.dwnl.spicehub.identity.application.model.AuthenticatedUser;
import br.com.dwnl.spicehub.identity.application.port.CurrentUserProvider;
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
class UpdateCartItemQuantityUseCaseTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartProductQueryPort cartProductQueryPort;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private AuthenticatedUser authenticatedUser;

    private UpdateCartItemQuantityUseCase useCase;

    private UUID userId;
    private UUID variantId;
    private Cart cart;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCartItemQuantityUseCase(
                cartRepository,
                cartProductQueryPort,
                currentUserProvider
        );

        userId = UUID.randomUUID();
        variantId = UUID.randomUUID();

        cart = Cart.create(userId);
        cart.addItem(variantId, 2);
    }

    private void mockAuthenticatedUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(authenticatedUser);
        when(authenticatedUser.id()).thenReturn(userId);
    }

    private void mockExistingCart() {
        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));
    }

    private void mockAvailableVariant() {
        when(cartProductQueryPort.findByVariantId(variantId))
                .thenReturn(Optional.of(new CartProductSnapshot(
                        UUID.randomUUID(),
                        variantId,
                        "Chá de Camomila",
                        new BigDecimal("15.90"),
                        true
                )));
    }

    @Test
    void shouldUpdateItemQuantity() {
        mockAuthenticatedUser();
        mockExistingCart();
        mockAvailableVariant();

        useCase.execute(variantId, 5);

        assertEquals(5, cart.getItems().getFirst().getQuantity());

        verify(cartRepository).save(cart);
    }

    @Test
    void shouldThrowWhenCartDoesNotExist() {
        mockAuthenticatedUser();

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                CartNotFoundException.class,
                () -> useCase.execute(variantId, 5)
        );

        verify(cartRepository, never()).save(any());
        verifyNoInteractions(cartProductQueryPort);
    }

    @Test
    void shouldThrowWhenItemDoesNotExistInCart() {
        mockAuthenticatedUser();
        mockExistingCart();

        UUID unknownVariantId = UUID.randomUUID();

        assertThrows(
                CartItemNotFoundException.class,
                () -> useCase.execute(unknownVariantId, 5)
        );

        verify(cartRepository, never()).save(any());
        verifyNoInteractions(cartProductQueryPort);
    }

    @Test
    void shouldRejectZeroQuantity() {
        mockAuthenticatedUser();
        mockExistingCart();

        assertThrows(
                InvalidCartItemQuantityException.class,
                () -> useCase.execute(variantId, 0)
        );

        verify(cartRepository, never()).save(any());
        verifyNoInteractions(cartProductQueryPort);
    }

    @Test
    void shouldRejectNegativeQuantity() {
        mockAuthenticatedUser();
        mockExistingCart();

        assertThrows(
                InvalidCartItemQuantityException.class,
                () -> useCase.execute(variantId, -1)
        );

        verify(cartRepository, never()).save(any());
        verifyNoInteractions(cartProductQueryPort);
    }

    @Test
    void shouldThrowWhenVariantDoesNotExist() {
        mockAuthenticatedUser();
        mockExistingCart();

        when(cartProductQueryPort.findByVariantId(variantId))
                .thenReturn(Optional.empty());

        assertThrows(
                CartVariantNotFoundException.class,
                () -> useCase.execute(variantId, 5)
        );

        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenVariantIsUnavailable() {
        mockAuthenticatedUser();
        mockExistingCart();

        when(cartProductQueryPort.findByVariantId(variantId))
                .thenReturn(Optional.of(new CartProductSnapshot(
                        UUID.randomUUID(),
                        variantId,
                        "Chá de Camomila",
                        new BigDecimal("15.90"),
                        false
                )));

        assertThrows(
                CartVariantUnavailableException.class,
                () -> useCase.execute(variantId, 5)
        );

        verify(cartRepository, never()).save(any());
    }
}
