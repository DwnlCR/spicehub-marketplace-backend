package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.storage.ImageStorage;
import br.com.dwnl.spicehub.catalog.infrastructure.exception.ImageStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProductImageUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageStorage imageStorage;

    @InjectMocks
    private GetProductImageUseCase useCase;

    private UUID productId;
    private Product product;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        product = Product.create(
                "Chá de Camomila",
                "Chá natural de camomila",
                UUID.randomUUID(),
                null
        );
    }

    @Test
    void shouldReturnProductImage() {
        String imageKey = "products/image.webp";
        byte[] imageBytes = {1, 2, 3, 4};

        product.changeImage(imageKey);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(imageStorage.download(imageKey))
                .thenReturn(imageBytes);

        Optional<byte[]> result = useCase.execute(productId);

        assertTrue(result.isPresent());
        assertArrayEquals(imageBytes, result.orElseThrow());

        verify(imageStorage).download(imageKey);
    }

    @Test
    void shouldReturnEmptyWhenProductHasNoImage() {
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        Optional<byte[]> result = useCase.execute(productId);

        assertTrue(result.isEmpty());

        verifyNoInteractions(imageStorage);
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> useCase.execute(productId)
        );

        verifyNoInteractions(imageStorage);
    }

    @Test
    void shouldPropagateStorageFailure() {
        String imageKey = "products/image.webp";

        product.changeImage(imageKey);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(imageStorage.download(imageKey))
                .thenThrow(new ImageStorageException(
                        "Failed to download product image",
                        new RuntimeException("S3 unavailable")
                ));

        assertThrows(
                ImageStorageException.class,
                () -> useCase.execute(productId)
        );
    }
}