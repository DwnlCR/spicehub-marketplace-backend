package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.image.ImageProcessor;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.storage.ImageStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UploadProductImageUseCase {

    private static final Logger log = LoggerFactory.getLogger(UploadProductImageUseCase.class);

    private final ProductRepository productRepository;
    private final ImageStorage imageStorage;
    private final ImageProcessor imageProcessor;

    public UploadProductImageUseCase(ProductRepository productRepository, ImageStorage imageStorage, ImageProcessor imageProcessor) {
        this.productRepository = productRepository;
        this.imageStorage = imageStorage;
        this.imageProcessor = imageProcessor;
    }

    public void execute(UUID productId, byte[] image) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));

        String previousImageKey = product.getImageKey();

        String newImageKey = "products/" + productId + "/" + UUID.randomUUID() + ".webp";

        byte[] processedImage = imageProcessor.process(image);

        imageStorage.upload(newImageKey, processedImage, "image/webp");

        try {
            product.changeImage(newImageKey);
            productRepository.save(product);
        }catch (RuntimeException exception){
            try {
                imageStorage.delete(newImageKey);
            }catch (RuntimeException cleanupException){
                exception.addSuppressed(cleanupException);
            }

            throw exception;
        }

        if (previousImageKey != null){
            try {
                imageStorage.delete(previousImageKey);
            }catch (RuntimeException exception){
                log.warn(
                        "Failed to delete previous product image: {}",
                        previousImageKey,
                        exception
                        );
            }

        }
    }
}
