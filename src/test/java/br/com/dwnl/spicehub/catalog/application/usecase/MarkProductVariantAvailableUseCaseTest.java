package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.InactiveProductException;
import br.com.dwnl.spicehub.catalog.application.exception.ProductVariantNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.MeasurementUnit;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkProductVariantAvailableUseCaseTest {

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private MarkProductVariantAvailableUseCase markProductVariantAvailableUseCase;

    @Test
    void shouldMarkVariantAsAvailableWhenProductIsActive() {
        Product product = Product.create(
                "Chá Verde",
                "Chá verde natural",
                UUID.randomUUID(),
                "products/cha-verde.jpg"
        );

        ProductVariant variant = ProductVariant.create(
                product.getId(),
                100,
                MeasurementUnit.GRAM,
                new BigDecimal("15.90")
        );

        variant.markSoldOut();

        when(variantRepository.findByIdAndProductId(
                variant.getId(),
                product.getId()
        )).thenReturn(Optional.of(variant));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(variantRepository.save(variant))
                .thenReturn(variant);

        ProductVariant result = markProductVariantAvailableUseCase.execute(
                variant.getId(),
                product.getId()
        );

        assertEquals(VariantAvailability.AVAILABLE, result.getAvailability());

        verify(variantRepository).findByIdAndProductId(
                variant.getId(),
                product.getId()
        );

        verify(productRepository).findById(product.getId());
        verify(variantRepository).save(variant);
    }

    @Test
    void shouldThrowWhenVariantDoesNotBelongToProduct() {
        UUID variantId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(variantRepository.findByIdAndProductId(variantId, productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductVariantNotFoundException.class,
                () -> markProductVariantAvailableUseCase.execute(
                        variantId,
                        productId
                )
        );

        verify(variantRepository).findByIdAndProductId(variantId, productId);
        verifyNoInteractions(productRepository);
        verify(variantRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenProductIsInactive() {
        Product product = Product.create(
                "Chá Verde",
                "Chá verde natural",
                UUID.randomUUID(),
                "products/cha-verde.jpg"
        );

        product.deactivate();

        ProductVariant variant = ProductVariant.create(
                product.getId(),
                100,
                MeasurementUnit.GRAM,
                new BigDecimal("15.90")
        );

        variant.markSoldOut();

        when(variantRepository.findByIdAndProductId(
                variant.getId(),
                product.getId()
        )).thenReturn(Optional.of(variant));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        assertThrows(
                InactiveProductException.class,
                () -> markProductVariantAvailableUseCase.execute(
                        variant.getId(),
                        product.getId()
                )
        );

        assertEquals(
                VariantAvailability.SOLD_OUT,
                variant.getAvailability()
        );

        verify(variantRepository).findByIdAndProductId(
                variant.getId(),
                product.getId()
        );

        verify(productRepository).findById(product.getId());
        verify(variantRepository, never()).save(any());
    }
}