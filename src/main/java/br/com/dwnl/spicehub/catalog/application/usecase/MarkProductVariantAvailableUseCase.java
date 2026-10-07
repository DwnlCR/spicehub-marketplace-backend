package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.InactiveProductException;
import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.application.exception.ProductVariantNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductStatus;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MarkProductVariantAvailableUseCase {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    public MarkProductVariantAvailableUseCase(ProductVariantRepository variantRepository, ProductRepository productRepository) {
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
    }

    public ProductVariant execute(UUID variantId, UUID productId){
        ProductVariant variant = variantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product variant no found"));

        Product product = productRepository.findById(variant.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (product.getStatus() != ProductStatus.ACTIVE){
            throw new InactiveProductException("Cannot make variant available while product is inactive");
        }

        variant.markAvailable();

        return variantRepository.save(variant);
    }
}
