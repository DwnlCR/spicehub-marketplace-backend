package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.CategoryNotFoundException;
import br.com.dwnl.spicehub.catalog.application.exception.InactiveCategoryException;
import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.Category;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.repository.CategoryRepository;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActivateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;

    public ActivateProductUseCase(ProductRepository productRepository, CategoryRepository categoryRepository, ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public Product execute(UUID productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        Category category = categoryRepository.findById(product.getCategoryId())
                        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        if (!category.isActive()){
            throw new InactiveCategoryException("Cannot activate product in an inactive category");
        }

        product.activate();

        List<ProductVariant> productVariants = productVariantRepository.findByProductId(productId);

        productVariants.forEach(productVariant -> {
            productVariant.markAvailable();
            productVariantRepository.save(productVariant);
        });

        return productRepository.save(product);
    }
}
