package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DeactivateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public DeactivateProductUseCase(ProductRepository productRepository, ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional
    public Product execute(UUID productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        product.deactivate();

        List<ProductVariant> productVariants = productVariantRepository.findByProductId(productId);

        productVariants.forEach(productVariant -> {
            productVariant.markSoldOut();
            productVariantRepository.save(productVariant);
        });

        return productRepository.save(product);
    }
}
