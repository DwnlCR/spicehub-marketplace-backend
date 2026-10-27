
package br.com.dwnl.spicehub.cart.infrastructure.catalog;

import br.com.dwnl.spicehub.cart.application.dto.CartProductSnapshot;
import br.com.dwnl.spicehub.catalog.domain.model.MeasurementUnit;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogCartProductAdapterTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductRepository productRepository;

    private CatalogCartProductAdapter adapter;

    private Product product;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        adapter = new CatalogCartProductAdapter(
                productVariantRepository,
                productRepository
        );

        product = Product.create(
                "Chamomile Tea",
                "Natural chamomile tea",
                UUID.randomUUID(),
                null
        );

        variant = ProductVariant.create(
                product.getId(),
                250,
                MeasurementUnit.GRAM,
                new BigDecimal("25.90")
        );
    }

    @Test
    void shouldReturnPurchasableSnapshotForActiveProductAndAvailableVariant() {
        mockVariantAndProduct();

        Optional<CartProductSnapshot> result =
                adapter.findByVariantId(variant.getId());

        assertTrue(result.isPresent());

        CartProductSnapshot snapshot = result.get();

        assertEquals(product.getId(), snapshot.productId());
        assertEquals(variant.getId(), snapshot.variantId());
        assertEquals("Chamomile Tea", snapshot.productName());
        assertEquals(0, new BigDecimal("25.90").compareTo(snapshot.price()));
        assertTrue(snapshot.purchasable());
    }

    @Test
    void shouldReturnUnavailableSnapshotForSoldOutVariant() {
        variant.markSoldOut();
        mockVariantAndProduct();

        Optional<CartProductSnapshot> result =
                adapter.findByVariantId(variant.getId());

        assertTrue(result.isPresent());
        assertFalse(result.get().purchasable());
    }

    @Test
    void shouldReturnUnavailableSnapshotForInactiveProduct() {
        product.deactivate();
        mockVariantAndProduct();

        Optional<CartProductSnapshot> result =
                adapter.findByVariantId(variant.getId());

        assertTrue(result.isPresent());
        assertFalse(result.get().purchasable());
    }

    @Test
    void shouldReturnUnavailableSnapshotForArchivedProduct() {
        product.archive();
        mockVariantAndProduct();

        Optional<CartProductSnapshot> result =
                adapter.findByVariantId(variant.getId());

        assertTrue(result.isPresent());
        assertFalse(result.get().purchasable());
    }

    @Test
    void shouldReturnEmptyWhenVariantDoesNotExist() {
        when(productVariantRepository.findById(variant.getId()))
                .thenReturn(Optional.empty());

        Optional<CartProductSnapshot> result =
                adapter.findByVariantId(variant.getId());

        assertTrue(result.isEmpty());

        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExist() {
        when(productVariantRepository.findById(variant.getId()))
                .thenReturn(Optional.of(variant));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.empty());

        Optional<CartProductSnapshot> result =
                adapter.findByVariantId(variant.getId());

        assertTrue(result.isEmpty());
    }

    private void mockVariantAndProduct() {
        when(productVariantRepository.findById(variant.getId()))
                .thenReturn(Optional.of(variant));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));
    }
}
