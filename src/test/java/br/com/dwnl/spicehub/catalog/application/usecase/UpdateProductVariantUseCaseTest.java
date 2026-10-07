package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.ProductVariantAlreadyExistsException;
import br.com.dwnl.spicehub.catalog.application.exception.ProductVariantNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.MeasurementUnit;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProductVariantUseCaseTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private UpdateProductVariantUseCase updateProductVariantUseCase;

    @Test
    void shouldUpdateProductVariant() {
        UUID productId = UUID.randomUUID();

        ProductVariant variant = ProductVariant.create(
                productId,
                100,
                MeasurementUnit.GRAM,
                new BigDecimal("12.90")
        );

        UUID variantId = variant.getId();

        when(productVariantRepository.findByIdAndProductId(variantId, productId))
                .thenReturn(Optional.of(variant));

        when(productVariantRepository
                .existsByProductIdAndQuantityAndMeasurementUnitAndIdNot(
                        productId,
                        250,
                        MeasurementUnit.GRAM,
                        variantId
                ))
                .thenReturn(false);

        when(productVariantRepository.save(any(ProductVariant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductVariant result = updateProductVariantUseCase.execute(
                variantId,
                productId,
                250,
                MeasurementUnit.GRAM,
                new BigDecimal("24.90")
        );

        assertEquals(250, result.getQuantity());
        assertEquals(MeasurementUnit.GRAM, result.getMeasurementUnit());
        assertEquals(
                0,
                new BigDecimal("24.90").compareTo(result.getPrice())
        );

        verify(productVariantRepository)
                .findByIdAndProductId(variantId, productId);

        verify(productVariantRepository).save(variant);
    }

    @Test
    void shouldThrowWhenProductVariantDoesNotExist() {
        UUID variantId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(productVariantRepository.findByIdAndProductId(variantId, productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductVariantNotFoundException.class,
                () -> updateProductVariantUseCase.execute(
                        variantId,
                        productId,
                        250,
                        MeasurementUnit.GRAM,
                        new BigDecimal("24.90")
                )
        );

        verify(productVariantRepository, never())
                .existsByProductIdAndQuantityAndMeasurementUnitAndIdNot(
                        any(),
                        anyInt(),
                        any(),
                        any()
                );

        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAnotherVariantHasSameMeasurement() {
        UUID productId = UUID.randomUUID();

        ProductVariant variant = ProductVariant.create(
                productId,
                100,
                MeasurementUnit.GRAM,
                new BigDecimal("12.90")
        );

        UUID variantId = variant.getId();

        when(productVariantRepository.findByIdAndProductId(variantId, productId))
                .thenReturn(Optional.of(variant));

        when(productVariantRepository
                .existsByProductIdAndQuantityAndMeasurementUnitAndIdNot(
                        productId,
                        250,
                        MeasurementUnit.GRAM,
                        variantId
                ))
                .thenReturn(true);

        assertThrows(
                ProductVariantAlreadyExistsException.class,
                () -> updateProductVariantUseCase.execute(
                        variantId,
                        productId,
                        250,
                        MeasurementUnit.GRAM,
                        new BigDecimal("24.90")
                )
        );

        assertEquals(100, variant.getQuantity());
        assertEquals(
                MeasurementUnit.GRAM,
                variant.getMeasurementUnit()
        );
        assertEquals(
                0,
                new BigDecimal("12.90").compareTo(variant.getPrice())
        );

        verify(productVariantRepository, never()).save(any());
    }
}