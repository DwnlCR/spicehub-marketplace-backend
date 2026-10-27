package br.com.dwnl.spicehub.cart.application.usecase;

import br.com.dwnl.spicehub.cart.application.dto.CartProductSnapshot;
import br.com.dwnl.spicehub.cart.application.exception.CartVariantNotFoundException;
import br.com.dwnl.spicehub.cart.application.exception.CartVariantUnavailableException;
import br.com.dwnl.spicehub.cart.application.port.CartProductQueryPort;
import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.domain.repository.CartRepository;
import br.com.dwnl.spicehub.identity.application.port.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddCartItemUseCase {

    private final CartRepository cartRepository;
    private final CartProductQueryPort cartProductQueryPort;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public void execute(UUID variantId, int quantity){
        UUID userId = currentUserProvider.getCurrentUser().id();

        CartProductSnapshot productSnapshot = cartProductQueryPort
                .findByVariantId(variantId)
                .orElseThrow(CartVariantNotFoundException::new);

        if (!productSnapshot.purchasable()){
            throw new CartVariantUnavailableException();
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.create(userId));

        cart.addItem(variantId, quantity);

        cartRepository.save(cart);
    }
}
