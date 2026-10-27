package br.com.dwnl.spicehub.cart.application.usecase;

import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.domain.repository.CartRepository;
import br.com.dwnl.spicehub.identity.application.port.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCartUseCase {

    private final CartRepository cartRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public Cart execute(){
        UUID userId = currentUserProvider.getCurrentUser().id();

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.create(userId));
    }
}
