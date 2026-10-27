package br.com.dwnl.spicehub.cart.domain.repository;

import br.com.dwnl.spicehub.cart.domain.model.Cart;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository {

    Optional<Cart> findByUserId(UUID userId);

    Cart save(Cart cart);
}
