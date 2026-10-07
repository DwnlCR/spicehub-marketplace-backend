package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.CategoryNotFoundException;
import br.com.dwnl.spicehub.catalog.application.exception.InactiveCategoryException;
import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.Category;
import br.com.dwnl.spicehub.catalog.domain.model.MeasurementUnit;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductStatus;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.model.VariantAvailability;
import br.com.dwnl.spicehub.catalog.domain.repository.CategoryRepository;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private ActivateProductUseCase activateProductUseCase;

    @Test
    void shouldActivateProductAndMarkAllVariantsAsAvailable() {
        Category category = Category.create("Chás");

        Product product = Product.create(
                "Chá Verde",
                "Chá verde natural",
                category.getId(),
                "products/cha-verde.jpg"
        );

        product.deactivate();

        ProductVariant variant100g = ProductVariant.create(
                product.getId(),
                100,
                MeasurementUnit.GRAM,
                new BigDecimal("15.90")
        );

        ProductVariant variant200g = ProductVariant.create(
                product.getId(),
                200,
                MeasurementUnit.GRAM,
                new BigDecimal("25.90")
        );

        variant100g.markSoldOut();
        variant200g.markSoldOut();

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findById(category.getId()))
                .thenReturn(Optional.of(category));

        when(productVariantRepository.findByProductId(product.getId()))
                .thenReturn(List.of(variant100g, variant200g));

        when(productRepository.save(product))
                .thenReturn(product);

        Product result = activateProductUseCase.execute(product.getId());

        assertEquals(ProductStatus.ACTIVE, result.getStatus());
        assertEquals(VariantAvailability.AVAILABLE, variant100g.getAvailability());
        assertEquals(VariantAvailability.AVAILABLE, variant200g.getAvailability());

        verify(productRepository).findById(product.getId());
        verify(categoryRepository).findById(category.getId());
        verify(productVariantRepository).findByProductId(product.getId());

        verify(productVariantRepository).save(variant100g);
        verify(productVariantRepository).save(variant200g);

        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> activateProductUseCase.execute(productId)
        );

        verify(productRepository).findById(productId);
        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(productVariantRepository);
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCategoryDoesNotExist() {
        UUID categoryId = UUID.randomUUID();

        Product product = Product.create(
                "Chá Verde",
                "Chá verde natural",
                categoryId,
                "products/cha-verde.jpg"
        );

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> activateProductUseCase.execute(product.getId())
        );

        verify(productRepository).findById(product.getId());
        verify(categoryRepository).findById(categoryId);

        verifyNoInteractions(productVariantRepository);
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCategoryIsInactive() {
        Category category = Category.create("Chás");
        category.deactivate();

        Product product = Product.create(
                "Chá Verde",
                "Chá verde natural",
                category.getId(),
                "products/cha-verde.jpg"
        );

        product.deactivate();

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findById(category.getId()))
                .thenReturn(Optional.of(category));

        assertThrows(
                InactiveCategoryException.class,
                () -> activateProductUseCase.execute(product.getId())
        );

        assertEquals(ProductStatus.INACTIVE, product.getStatus());

        verify(productRepository).findById(product.getId());
        verify(categoryRepository).findById(category.getId());

        verifyNoInteractions(productVariantRepository);
        verify(productRepository, never()).save(any());
    }
}