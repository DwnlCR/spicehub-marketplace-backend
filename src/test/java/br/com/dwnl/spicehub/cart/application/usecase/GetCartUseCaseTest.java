package br.com.dwnl.spicehub.cart.application.usecase;

import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.domain.repository.CartRepository;
import br.com.dwnl.spicehub.identity.application.model.AuthenticatedUser;
import br.com.dwnl.spicehub.identity.application.port.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCartUseCaseTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private AuthenticatedUser authenticatedUser;

    private GetCartUseCase useCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        useCase = new GetCartUseCase(
                cartRepository,
                currentUserProvider
        );

        userId = UUID.randomUUID();
    }

    @Test
    void shouldReturnExistingCart() {
        Cart cart = Cart.create(userId);
        UUID variantId = UUID.randomUUID();

        cart.addItem(variantId, 2);

        when(authenticatedUser.id()).thenReturn(userId);
        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        Cart result = useCase.execute();

        assertEquals(cart.getId(), result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(1, result.getItems().size());
        assertEquals(variantId, result.getItems().getFirst().getVariantId());
        assertEquals(2, result.getItems().getFirst().getQuantity());

        verify(cartRepository).findByUserId(userId);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldReturnEmptyCartWhenUserHasNoCart() {
        when(authenticatedUser.id()).thenReturn(userId);
        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        Cart result = useCase.execute();

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertTrue(result.getItems().isEmpty());

        verify(cartRepository).findByUserId(userId);
        verify(cartRepository, never()).save(any());
    }
}
