package br.com.dwnl.spicehub.cart.application.usecase;

import br.com.dwnl.spicehub.cart.application.exception.CartNotFoundException;
import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.domain.repository.CartRepository;
import br.com.dwnl.spicehub.identity.application.port.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemoveCartItemUseCase {

    private final CartRepository cartRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public void execute(UUID variantId){
        UUID userId = currentUserProvider.getCurrentUser().id();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for the authenticated user"));

        cart.removeItem(variantId);
        cartRepository.save(cart);
    }
}
