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
import br.com.dwnl.spicehub.identity.application.port.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCartItemQuantityUseCase {

    private final CartRepository cartRepository;
    private final CartProductQueryPort cartProductQueryPort;
    private final CurrentUserProvider currentUserProvider;


    @Transactional
    public void execute(UUID variantId, int quantity){
        UUID userId = currentUserProvider.getCurrentUser().id();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(
                        "Cart not found for the authenticated user"
                ));

        if (cart.getItems().stream().noneMatch(item -> item.getVariantId().equals(variantId))){
            throw new CartItemNotFoundException("Cart item not found");
        }

        if (quantity <= 0){
            throw new InvalidCartItemQuantityException("Cart item quantity must be greater than zero");
        }

        CartProductSnapshot snapshot = cartProductQueryPort.findByVariantId(variantId)
                .orElseThrow(CartVariantNotFoundException::new);

        if (!snapshot.purchasable()){
            throw new CartVariantUnavailableException();
        }

        cart.changeItemQuantity(variantId ,quantity);
        cartRepository.save(cart);
    }
}
