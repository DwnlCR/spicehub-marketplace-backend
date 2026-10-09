package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.storage.ImageStorage;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetProductImageUseCase {

    private final ProductRepository productRepository;
    private final ImageStorage imageStorage;

    public GetProductImageUseCase(ProductRepository productRepository, ImageStorage imageStorage) {
        this.productRepository = productRepository;
        this.imageStorage = imageStorage;
    }

    public Optional<byte[]> execute(UUID productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));

        String imageKey = product.getImageKey();

        if (imageKey == null){
            return Optional.empty();
        }

        return Optional.of(imageStorage.download(imageKey));
    }
}
