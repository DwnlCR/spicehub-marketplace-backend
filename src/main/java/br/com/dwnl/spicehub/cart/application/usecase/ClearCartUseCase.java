package br.com.dwnl.spicehub.cart.application.usecase;

import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.domain.repository.CartRepository;
import br.com.dwnl.spicehub.identity.application.port.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClearCartUseCase {

    private final CartRepository cartRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public void execute(){
        UUID userId = currentUserProvider.getCurrentUser().id();

        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);

        if (optionalCart.isEmpty()){
            return;
        }

        Cart cart = optionalCart.get();

        if (cart.getItems().isEmpty()){
            return;
        }

        cart.clear();
        cartRepository.save(cart);
    }
}
