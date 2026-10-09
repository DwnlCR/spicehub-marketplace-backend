
package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.InactiveCategoryException;
import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.Category;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.repository.CategoryRepository;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private UpdateProductUseCase updateProductUseCase;

    @Test
    void shouldUpdateProductWithoutChangingCategory() {
        UUID categoryId = UUID.randomUUID();

        Product product = Product.create(
                "Chá Verde",
                "Descrição antiga",
                categoryId,
                "old-image.jpg"
        );

        UUID productId = product.getId();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(productRepository.save(product))
                .thenReturn(product);

        Product result = updateProductUseCase.execute(
                productId,
                "Chá Verde Premium",
                "Nova descrição",
                categoryId
        );

        assertEquals("Chá Verde Premium", result.getName());
        assertEquals("Nova descrição", result.getDescription());
        assertEquals(categoryId, result.getCategoryId());

        // A imagem anterior deve permanecer inalterada.
        assertEquals("old-image.jpg", result.getImageKey());

        verify(categoryRepository, never()).findById(any());
        verify(productRepository).save(product);
    }

    @Test
    void shouldUpdateProductAndChangeCategoryWhenNewCategoryIsActive() {
        UUID oldCategoryId = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();

        Product product = Product.create(
                "Chá Verde",
                "Descrição",
                oldCategoryId,
                "image.jpg"
        );

        Category newCategory = Category.restore(
                newCategoryId,
                "Ervas",
                true,
                Instant.now(),
                Instant.now()
        );

        UUID productId = product.getId();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findById(newCategoryId))
                .thenReturn(Optional.of(newCategory));

        when(productRepository.save(product))
                .thenReturn(product);

        Product result = updateProductUseCase.execute(
                productId,
                "Chá Verde",
                "Descrição atualizada",
                newCategoryId
        );

        assertEquals(newCategoryId, result.getCategoryId());
        assertEquals("Descrição atualizada", result.getDescription());
        assertEquals("image.jpg", result.getImageKey());

        verify(categoryRepository).findById(newCategoryId);
        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> updateProductUseCase.execute(
                        productId,
                        "Chá Verde",
                        "Descrição",
                        categoryId
                )
        );

        verify(categoryRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenNewCategoryIsInactive() {
        UUID oldCategoryId = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();

        Product product = Product.create(
                "Chá Verde",
                "Descrição",
                oldCategoryId,
                "image.jpg"
        );

        Category inactiveCategory = Category.restore(
                newCategoryId,
                "Ervas",
                false,
                Instant.now(),
                Instant.now()
        );

        UUID productId = product.getId();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findById(newCategoryId))
                .thenReturn(Optional.of(inactiveCategory));

        assertThrows(
                InactiveCategoryException.class,
                () -> updateProductUseCase.execute(
                        productId,
                        "Chá Verde",
                        "Descrição",
                        newCategoryId
                )
        );

        assertEquals(oldCategoryId, product.getCategoryId());
        assertEquals("image.jpg", product.getImageKey());

        verify(productRepository, never()).save(any());
    }
}
