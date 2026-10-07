package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.application.exception.ProductVariantAlreadyExistsException;
import br.com.dwnl.spicehub.catalog.domain.model.MeasurementUnit;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductVariantUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private CreateProductVariantUseCase createProductVariantUseCase;

    @Test
    void shouldCreateProductVariant() {
        Product product = Product.create(
                "Chá Verde",
                "Chá verde natural",
                UUID.randomUUID(),
                "products/cha-verde.jpg"
        );

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(productVariantRepository
                .existsByProductIdAndQuantityAndMeasurementUnit(
                        product.getId(),
                        100,
                        MeasurementUnit.GRAM
                ))
                .thenReturn(false);

        when(productVariantRepository.save(any(ProductVariant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductVariant result = createProductVariantUseCase.execute(
                product.getId(),
                100,
                MeasurementUnit.GRAM,
                new BigDecimal("15.90")
        );

        assertEquals(product.getId(), result.getProductId());
        assertEquals(100, result.getQuantity());
        assertEquals(MeasurementUnit.GRAM, result.getMeasurementUnit());
        assertEquals(0, new BigDecimal("15.90").compareTo(result.getPrice()));

        verify(productRepository).findById(product.getId());

        verify(productVariantRepository)
                .existsByProductIdAndQuantityAndMeasurementUnit(
                        product.getId(),
                        100,
                        MeasurementUnit.GRAM
                );

        verify(productVariantRepository).save(any(ProductVariant.class));
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> createProductVariantUseCase.execute(
                        productId,
                        100,
                        MeasurementUnit.GRAM,
                        new BigDecimal("15.90")
                )
        );

        verify(productRepository).findById(productId);

        verifyNoInteractions(productVariantRepository);
    }

    @Test
    void shouldThrowWhenProductVariantAlreadyExists() {
        Product product = Product.create(
                "Chá Verde",
                "Chá verde natural",
                UUID.randomUUID(),
                "products/cha-verde.jpg"
        );

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(productVariantRepository
                .existsByProductIdAndQuantityAndMeasurementUnit(
                        product.getId(),
                        100,
                        MeasurementUnit.GRAM
                ))
                .thenReturn(true);

        assertThrows(
                ProductVariantAlreadyExistsException.class,
                () -> createProductVariantUseCase.execute(
                        product.getId(),
                        100,
                        MeasurementUnit.GRAM,
                        new BigDecimal("15.90")
                )
        );

        verify(productRepository).findById(product.getId());

        verify(productVariantRepository)
                .existsByProductIdAndQuantityAndMeasurementUnit(
                        product.getId(),
                        100,
                        MeasurementUnit.GRAM
                );

        verify(productVariantRepository, never()).save(any());
    }
}