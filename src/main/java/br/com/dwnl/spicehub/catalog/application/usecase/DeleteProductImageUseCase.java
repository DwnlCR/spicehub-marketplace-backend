package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.storage.ImageStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteProductImageUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteProductImageUseCase.class);

    private final ProductRepository productRepository;
    private final ImageStorage imageStorage;

    public DeleteProductImageUseCase(ProductRepository productRepository, ImageStorage imageStorage) {
        this.productRepository = productRepository;
        this.imageStorage = imageStorage;
    }

    public void execute(UUID productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));

        String imageKey = product.getImageKey();

        if (imageKey == null){
            return;
        }

        product.changeImage(null);
        productRepository.save(product);

        try {
            imageStorage.delete(imageKey);
        }catch (RuntimeException exception){
            log.warn(
                    "Failed to delete product image from storage: {}",
                    imageKey,
                    exception
            );
        }

    }
}
