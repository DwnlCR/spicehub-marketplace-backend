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

    private final GetProductUseCase getProductUseCase;
    private final ImageStorage imageStorage;

    public GetProductImageUseCase(GetProductUseCase getProductUseCase, ImageStorage imageStorage) {
        this.getProductUseCase = getProductUseCase;
        this.imageStorage = imageStorage;
    }

    public Optional<byte[]> execute(UUID productId){
        Product product = getProductUseCase.execute(productId);

        String imageKey = product.getImageKey();

        if (imageKey == null){
            return Optional.empty();
        }

        return Optional.of(imageStorage.download(imageKey));
    }
}
