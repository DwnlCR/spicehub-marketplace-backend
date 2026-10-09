package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.CategoryNotFoundException;
import br.com.dwnl.spicehub.catalog.application.exception.InactiveCategoryException;
import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.Category;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.repository.CategoryRepository;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public UpdateProductUseCase(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Product execute(UUID productId, String name, String description, UUID categoryId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (!product.getCategoryId().equals(categoryId)){
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

            if (!category.isActive()){
                throw new InactiveCategoryException("Cannot move product to an inactive category");
            }
            product.changeCategory(categoryId);
        }
        product.rename(name);
        product.changeDescription(description);

        return productRepository.save(product);
    }
}
