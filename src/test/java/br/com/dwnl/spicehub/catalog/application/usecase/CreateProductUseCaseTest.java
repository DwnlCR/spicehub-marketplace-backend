package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.CategoryNotFoundException;
import br.com.dwnl.spicehub.catalog.application.exception.InactiveCategoryException;
import br.com.dwnl.spicehub.catalog.domain.model.Category;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.repository.CategoryRepository;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CreateProductUseCase createProductUseCase;

    @Test
    void shouldCreateProductWhenCategoryIsActive() {
        UUID categoryId = UUID.randomUUID();

        Category category = Category.create("Chás");

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result = createProductUseCase.execute(
                "Chá Verde",
                "Chá verde natural",
                categoryId,
                "products/cha-verde.jpg"
        );

        assertEquals("Chá Verde", result.getName());
        assertEquals("Chá verde natural", result.getDescription());
        assertEquals(categoryId, result.getCategoryId());
        assertEquals("products/cha-verde.jpg", result.getImageKey());

        verify(categoryRepository).findById(categoryId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowWhenCategoryDoesNotExist() {
        UUID categoryId = UUID.randomUUID();

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> createProductUseCase.execute(
                        "Chá Verde",
                        "Chá verde natural",
                        categoryId,
                        "products/cha-verde.jpg"
                )
        );

        verify(categoryRepository).findById(categoryId);
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCategoryIsInactive() {
        UUID categoryId = UUID.randomUUID();

        Category category = Category.create("Chás");
        category.deactivate();

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        assertThrows(
                InactiveCategoryException.class,
                () -> createProductUseCase.execute(
                        "Chá Verde",
                        "Chá verde natural",
                        categoryId,
                        "products/cha-verde.jpg"
                )
        );

        verify(categoryRepository).findById(categoryId);
        verify(productRepository, never()).save(any());
    }
}