
package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.domain.image.ImageProcessor;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.storage.ImageStorage;
import br.com.dwnl.spicehub.catalog.infrastructure.image.ProductImageProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadProductImageUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageStorage imageStorage;

    @Mock
    private ImageProcessor imageProcessor;

    @InjectMocks
    private UploadProductImageUseCase useCase;

    private UUID productId;
    private Product product;

    private final ProductImageProcessor processor = new ProductImageProcessor();

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        product = Product.create(
                "Camomila",
                "Chá de camomila",
                UUID.randomUUID(),
                null
        );
    }

    @Test
    void shouldUploadAndSaveProductImage() {
        byte[] originalImage = {1, 2, 3};
        byte[] processedImage = {4, 5, 6};

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(imageProcessor.process(originalImage))
                .thenReturn(processedImage);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(productId, originalImage);

        ArgumentCaptor<String> keyCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(imageStorage).upload(
                keyCaptor.capture(),
                same(processedImage),
                eq("image/webp")
        );

        String imageKey = keyCaptor.getValue();

        assertTrue(imageKey.startsWith("products/" + productId + "/"));
        assertTrue(imageKey.endsWith(".webp"));
        assertEquals(imageKey, product.getImageKey());

        verify(productRepository).save(product);
        verify(imageStorage, never()).delete(anyString());
    }


    @Test
    void shouldDeleteNewImageWhenOptimisticLockingFails() {
        byte[] originalImage = {1, 2, 3};
        byte[] processedImage = {4, 5, 6};

        String previousImageKey = "products/previous-image.webp";

        product.changeImage(previousImageKey);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(imageProcessor.process(originalImage))
                .thenReturn(processedImage);

        when(productRepository.save(any(Product.class)))
                .thenThrow(
                        new ObjectOptimisticLockingFailureException(
                                Product.class,
                                productId
                        )
                );

        assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> useCase.execute(productId, originalImage)
        );

        ArgumentCaptor<String> keyCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(imageStorage).upload(
                keyCaptor.capture(),
                same(processedImage),
                eq("image/webp")
        );

        String newImageKey = keyCaptor.getValue();

        verify(imageStorage).delete(newImageKey);

        verify(imageStorage, never()).delete(previousImageKey);

        verify(productRepository).save(product);
    }

        @Test
        void shouldConvertPngToWebP() throws Exception {
            BufferedImage original = new BufferedImage(1600, 900, BufferedImage.TYPE_INT_RGB);

            Graphics2D graphics = original.createGraphics();

            try {
                graphics.setColor(Color.GREEN);
                graphics.fillRect(0, 0, 1600, 900);
            } finally {
                graphics.dispose();
            }

            ByteArrayOutputStream input = new ByteArrayOutputStream();

            assertTrue(ImageIO.write(original, "png", input));

            byte[] result = processor.process(input.toByteArray());

            assertNotNull(result);
            assertTrue(result.length > 0);

            assertEquals("RIFF", new String(result, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));

            assertEquals("WEBP", new String(result, 8, 4, java.nio.charset.StandardCharsets.US_ASCII));

            BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(result));

            assertNotNull(decoded);
            assertEquals(1200, decoded.getWidth());
            assertEquals(675, decoded.getHeight());
        }

    @Test
    void shouldDeletePreviousImageAfterSuccessfulUpload() {
        byte[] originalImage = {1, 2, 3};
        byte[] processedImage = {4, 5, 6};

        String previousImageKey = "products/previous-image.webp";
        product.changeImage(previousImageKey);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(imageProcessor.process(originalImage))
                .thenReturn(processedImage);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(productId, originalImage);

        ArgumentCaptor<String> keyCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(imageStorage).upload(
                keyCaptor.capture(),
                same(processedImage),
                eq("image/webp")
        );

        String newImageKey = keyCaptor.getValue();

        assertEquals(newImageKey, product.getImageKey());

        InOrder inOrder = inOrder(
                imageStorage,
                productRepository
        );

        inOrder.verify(imageStorage).upload(
                eq(newImageKey),
                same(processedImage),
                eq("image/webp")
        );

        inOrder.verify(productRepository).save(product);

        inOrder.verify(imageStorage).delete(previousImageKey);

        verify(imageStorage, never()).delete(newImageKey);
    }

}
