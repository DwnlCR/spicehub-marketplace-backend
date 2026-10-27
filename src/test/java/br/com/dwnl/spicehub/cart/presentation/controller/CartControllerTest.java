
package br.com.dwnl.spicehub.cart.presentation.controller;

import br.com.dwnl.spicehub.cart.application.usecase.AddCartItemUseCase;
import br.com.dwnl.spicehub.cart.application.usecase.GetCartUseCase;
import br.com.dwnl.spicehub.cart.application.usecase.UpdateCartItemQuantityUseCase;
import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.presentation.dto.AddCartItemRequest;
import br.com.dwnl.spicehub.cart.presentation.dto.CartResponse;
import br.com.dwnl.spicehub.cart.presentation.dto.UpdateCartItemQuantityRequest;
import br.com.dwnl.spicehub.cart.presentation.mapper.CartResponseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private GetCartUseCase getCartUseCase;

    @Mock
    private AddCartItemUseCase addCartItemUseCase;

    @Mock
    private UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;

    @Mock
    private CartResponseMapper cartResponseMapper;

    @InjectMocks
    private CartController cartController;

    private Cart cart;
    private CartResponse response;

    @BeforeEach
    void setUp() {
        cart = Cart.create(UUID.randomUUID());

        response = new CartResponse(
                cart.getId(),
                List.of(),
                0,
                BigDecimal.ZERO
        );
    }

    @Test
    void shouldReturnCartSuccessfully() {
        when(getCartUseCase.execute()).thenReturn(cart);
        when(cartResponseMapper.toResponse(cart)).thenReturn(response);

        ResponseEntity<CartResponse> result = cartController.getCart();

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(cart.getId(), result.getBody().id());
        assertTrue(result.getBody().items().isEmpty());
        assertEquals(0, result.getBody().totalItems());
        assertEquals(0, result.getBody().totalPrice().compareTo(BigDecimal.ZERO));

        verify(getCartUseCase).execute();
        verify(cartResponseMapper).toResponse(cart);
    }

    @Test
    void shouldPropagateGetCartException() {
        RuntimeException exception = new RuntimeException("Unexpected error");

        when(getCartUseCase.execute()).thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> cartController.getCart()
        );

        assertSame(exception, thrown);
        verifyNoInteractions(cartResponseMapper);
    }

    @Test
    void shouldAddItemSuccessfully() {
        UUID variantId = UUID.randomUUID();
        AddCartItemRequest request = new AddCartItemRequest(variantId, 2);

        ResponseEntity<Void> result = cartController.addItem(request);

        assertEquals(204, result.getStatusCode().value());
        assertNull(result.getBody());

        verify(addCartItemUseCase).execute(variantId, 2);
    }

    @Test
    void shouldPropagateAddItemException() {
        UUID variantId = UUID.randomUUID();
        AddCartItemRequest request = new AddCartItemRequest(variantId, 2);

        RuntimeException exception = new RuntimeException("Unexpected error");

        doThrow(exception)
                .when(addCartItemUseCase)
                .execute(variantId, 2);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> cartController.addItem(request)
        );

        assertSame(exception, thrown);
    }

    @Test
    void shouldUpdateItemQuantitySuccessfully() {
        UUID variantId = UUID.randomUUID();
        UpdateCartItemQuantityRequest request =
                new UpdateCartItemQuantityRequest(5);

        ResponseEntity<Void> result =
                cartController.updateItemQuantity(variantId, request);

        assertEquals(204, result.getStatusCode().value());
        assertNull(result.getBody());

        verify(updateCartItemQuantityUseCase).execute(variantId, 5);
    }

    @Test
    void shouldPropagateUpdateItemQuantityException() {
        UUID variantId = UUID.randomUUID();
        UpdateCartItemQuantityRequest request =
                new UpdateCartItemQuantityRequest(5);

        RuntimeException exception = new RuntimeException("Unexpected error");

        doThrow(exception)
                .when(updateCartItemQuantityUseCase)
                .execute(variantId, 5);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> cartController.updateItemQuantity(variantId, request)
        );

        assertSame(exception, thrown);
        verify(updateCartItemQuantityUseCase).execute(variantId, 5);
    }
}
