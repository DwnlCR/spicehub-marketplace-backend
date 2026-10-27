
package br.com.dwnl.spicehub.cart.application.usecase;

import br.com.dwnl.spicehub.cart.application.dto.CartProductSnapshot;
import br.com.dwnl.spicehub.cart.application.exception.CartVariantNotFoundException;
import br.com.dwnl.spicehub.cart.application.exception.CartVariantUnavailableException;
import br.com.dwnl.spicehub.cart.application.port.CartProductQueryPort;
import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.domain.repository.CartRepository;
import br.com.dwnl.spicehub.identity.application.model.AuthenticatedUser;
import br.com.dwnl.spicehub.identity.application.port.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddCartItemUseCaseTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartProductQueryPort cartProductQueryPort;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private AddCartItemUseCase useCase;

    private UUID userId;
    private UUID variantId;

    @BeforeEach
    void setUp() {
        useCase = new AddCartItemUseCase(
                cartRepository,
                cartProductQueryPort,
                currentUserProvider
        );

        userId = UUID.randomUUID();
        variantId = UUID.randomUUID();
    }

    @Test
    void shouldCreateCartAndAddItem() {
        mockAuthenticatedUser();
        mockPurchasableVariant();

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        useCase.execute(variantId, 2);

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());

        Cart savedCart = captor.getValue();

        assertEquals(userId, savedCart.getUserId());
        assertEquals(1, savedCart.getItems().size());
        assertEquals(variantId, savedCart.getItems().getFirst().getVariantId());
        assertEquals(2, savedCart.getItems().getFirst().getQuantity());
    }

    @Test
    void shouldAddItemToExistingCart() {
        mockAuthenticatedUser();
        mockPurchasableVariant();

        Cart cart = Cart.create(userId);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        useCase.execute(variantId, 3);

        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().getFirst().getQuantity());

        verify(cartRepository).save(cart);
    }

    @Test
    void shouldAccumulateQuantityForExistingVariant() {
        mockAuthenticatedUser();
        mockPurchasableVariant();

        Cart cart = Cart.create(userId);
        cart.addItem(variantId, 2);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        useCase.execute(variantId, 3);

        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().getFirst().getQuantity());

        verify(cartRepository).save(cart);
    }

    @Test
    void shouldRejectNonexistentVariant() {
        mockAuthenticatedUser();

        when(cartProductQueryPort.findByVariantId(variantId))
                .thenReturn(Optional.empty());

        assertThrows(
                CartVariantNotFoundException.class,
                () -> useCase.execute(variantId, 2)
        );

        verifyNoInteractions(cartRepository);
    }

    @Test
    void shouldRejectUnavailableVariant() {
        mockAuthenticatedUser();

        when(cartProductQueryPort.findByVariantId(variantId))
                .thenReturn(Optional.of(snapshot(false)));

        assertThrows(
                CartVariantUnavailableException.class,
                () -> useCase.execute(variantId, 2)
        );

        verifyNoInteractions(cartRepository);
    }

    @Test
    void shouldRejectInvalidQuantityWithoutSaving() {
        mockAuthenticatedUser();
        mockPurchasableVariant();

        Cart cart = Cart.create(userId);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        assertThrows(
                br.com.dwnl.spicehub.cart.domain.exception.InvalidCartItemQuantityException.class,
                () -> useCase.execute(variantId, 0)
        );

        verify(cartRepository, never()).save(any());
    }

    private void mockAuthenticatedUser() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new AuthenticatedUser(
                        userId,
                        "customer@example.com",
                        Set.of()
                ));
    }

    private void mockPurchasableVariant() {
        when(cartProductQueryPort.findByVariantId(variantId))
                .thenReturn(Optional.of(snapshot(true)));
    }

    private CartProductSnapshot snapshot(boolean purchasable) {
        return new CartProductSnapshot(
                UUID.randomUUID(),
                variantId,
                "Chamomile Tea",
                new BigDecimal("25.90"),
                purchasable
        );
    }
}
