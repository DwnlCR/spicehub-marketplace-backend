package br.com.dwnl.spicehub.cart.application.usecase;

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
class ClearCartUseCaseTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ClearCartUseCase clearCartUseCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void shouldClearCartSuccessfully() {
        Cart cart = Cart.create(userId);

        cart.addItem(UUID.randomUUID(), 2);
        cart.addItem(UUID.randomUUID(), 3);

        AuthenticatedUser authenticatedUser = mockAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        clearCartUseCase.execute();

        assertTrue(cart.getItems().isEmpty());

        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(cart);
    }

    @Test
    void shouldNotSaveWhenCartIsAlreadyEmpty() {
        Cart cart = Cart.create(userId);

        AuthenticatedUser authenticatedUser = mockAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        clearCartUseCase.execute();

        assertTrue(cart.getItems().isEmpty());

        verify(cartRepository).findByUserId(userId);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldNotThrowWhenCartDoesNotExist() {
        AuthenticatedUser authenticatedUser = mockAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> clearCartUseCase.execute());

        verify(cartRepository).findByUserId(userId);
        verify(cartRepository, never()).save(any());
    }

    private AuthenticatedUser mockAuthenticatedUser() {
        AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);

        when(authenticatedUser.id()).thenReturn(userId);

        return authenticatedUser;
    }
}