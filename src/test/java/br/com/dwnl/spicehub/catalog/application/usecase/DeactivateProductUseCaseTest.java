package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.MeasurementUnit;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductStatus;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.model.VariantAvailability;
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
class DeactivateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private DeactivateProductUseCase deactivateProductUseCase;

    @Test
    void shouldDeactivateProductAndMarkAllVariantsAsSoldOut() {
        UUID categoryId = UUID.randomUUID();

        Product product = Product.create(
                "Chá Verde",
                "Chá verde natural",
                categoryId,
                "products/cha-verde.jpg"
        );

        UUID productId = product.getId();

        ProductVariant variant100g = ProductVariant.create(
                productId,
                100,
                MeasurementUnit.GRAM,
                new BigDecimal("15.90")
        );

        ProductVariant variant200g = ProductVariant.create(
                productId,
                200,
                MeasurementUnit.GRAM,
                new BigDecimal("25.90")
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findByProductId(productId))
                .thenReturn(List.of(variant100g, variant200g));

        when(productRepository.save(product))
                .thenReturn(product);

        Product result = deactivateProductUseCase.execute(productId);

        assertEquals(ProductStatus.INACTIVE, result.getStatus());
        assertEquals(VariantAvailability.SOLD_OUT, variant100g.getAvailability());
        assertEquals(VariantAvailability.SOLD_OUT, variant200g.getAvailability());

        verify(productRepository).findById(productId);
        verify(productVariantRepository).findByProductId(productId);

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
                () -> deactivateProductUseCase.execute(productId)
        );

        verify(productRepository).findById(productId);
        verify(productVariantRepository, never()).findByProductId(any());
        verify(productVariantRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }
}