package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.ProductVariantNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MarkProductVariantSoldOutUseCase {

    private final ProductVariantRepository productVariantRepository;

    public MarkProductVariantSoldOutUseCase(ProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    public ProductVariant execute(UUID variantId, UUID productId){
        ProductVariant variant = productVariantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product variant not found"));

        variant.markSoldOut();

        return productVariantRepository.save(variant);
    }
}
