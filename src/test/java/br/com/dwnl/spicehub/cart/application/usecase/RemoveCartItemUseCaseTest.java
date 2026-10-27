package br.com.dwnl.spicehub.cart.application.usecase;

import br.com.dwnl.spicehub.cart.application.exception.CartNotFoundException;
import br.com.dwnl.spicehub.cart.domain.exception.CartItemNotFoundException;
import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.domain.repository.CartRepository;
import br.com.dwnl.spicehub.identity.application.model.AuthenticatedUser;
import br.com.dwnl.spicehub.identity.application.port.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveCartItemUseCaseTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private RemoveCartItemUseCase removeCartItemUseCase;

    private UUID userId;
    private UUID variantId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        variantId = UUID.randomUUID();
    }

    @Test
    void shouldRemoveItemFromCartSuccessfully() {
        Cart cart = Cart.create(userId);
        cart.addItem(variantId, 2);

        AuthenticatedUser authenticatedUser = mockAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        removeCartItemUseCase.execute(variantId);

        assertTrue(cart.getItems().isEmpty());

        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(cart);
    }

    @Test
    void shouldThrowExceptionWhenCartDoesNotExist() {
        AuthenticatedUser authenticatedUser = mockAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                CartNotFoundException.class,
                () -> removeCartItemUseCase.execute(variantId)
        );

        verify(cartRepository).findByUserId(userId);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenItemDoesNotExist() {
        Cart cart = Cart.create(userId);

        AuthenticatedUser authenticatedUser = mockAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        assertThrows(
                CartItemNotFoundException.class,
                () -> removeCartItemUseCase.execute(variantId)
        );

        assertTrue(cart.getItems().isEmpty());

        verify(cartRepository).findByUserId(userId);
        verify(cartRepository, never()).save(any());
    }

    private AuthenticatedUser mockAuthenticatedUser() {
        AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);

        when(authenticatedUser.id()).thenReturn(userId);

        return authenticatedUser;
    }
}