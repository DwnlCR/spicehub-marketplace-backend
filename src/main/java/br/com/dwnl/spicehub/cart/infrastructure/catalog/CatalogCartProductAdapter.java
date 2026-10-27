package br.com.dwnl.spicehub.cart.infrastructure.catalog;

import br.com.dwnl.spicehub.cart.application.dto.CartProductSnapshot;
import br.com.dwnl.spicehub.cart.application.port.CartProductQueryPort;
import br.com.dwnl.spicehub.catalog.domain.model.ProductStatus;
import br.com.dwnl.spicehub.catalog.domain.model.VariantAvailability;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CatalogCartProductAdapter implements CartProductQueryPort {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<CartProductSnapshot> findByVariantId(UUID variantId) {
        return productVariantRepository.findById(variantId)
                .flatMap(variant -> productRepository.findById(variant.getProductId())
                .map(product -> new CartProductSnapshot(
                        product.getId(),
                        variant.getId(),
                        product.getName(),
                        variant.getPrice(),
                        product.getStatus() == ProductStatus.ACTIVE && variant.getAvailability() == VariantAvailability.AVAILABLE
                )));
    }
}
