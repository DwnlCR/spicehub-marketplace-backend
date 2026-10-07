package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListProductVariantUseCase {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    public ListProductVariantUseCase(ProductRepository productRepository, ProductVariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    public List<ProductVariant> execute(UUID productId){
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        return variantRepository.findByProductId(productId);
    }
}
