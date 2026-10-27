package br.com.dwnl.spicehub.cart.application.port;

import br.com.dwnl.spicehub.cart.application.dto.CartProductSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface CartProductQueryPort {

    Optional<CartProductSnapshot> findByVariantId(UUID variantId);
}
