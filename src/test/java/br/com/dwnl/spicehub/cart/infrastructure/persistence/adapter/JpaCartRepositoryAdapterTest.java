
package br.com.dwnl.spicehub.cart.infrastructure.persistence.adapter;

import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.infrastructure.persistence.entity.CartJpaEntity;
import br.com.dwnl.spicehub.cart.infrastructure.persistence.mapper.CartJpaMapper;
import br.com.dwnl.spicehub.cart.infrastructure.persistence.repository.SpringDataCartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaCartRepositoryAdapterTest {

    @Mock
    private SpringDataCartRepository springDataCartRepository;

    private CartJpaMapper mapper;
    private JpaCartRepositoryAdapter adapter;

    private UUID userId;

    @BeforeEach
    void setUp() {
        mapper = new CartJpaMapper();

        adapter = new JpaCartRepositoryAdapter(
                springDataCartRepository,
                mapper
        );

        userId = UUID.randomUUID();
    }

    @Test
    void shouldReturnEmptyWhenCartDoesNotExist() {
        when(springDataCartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        Optional<Cart> result = adapter.findByUserId(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindExistingCartByUserId() {
        Cart cart = Cart.create(userId);
        cart.addItem(UUID.randomUUID(), 2);

        CartJpaEntity entity = mapper.toEntity(cart);

        when(springDataCartRepository.findByUserId(userId))
                .thenReturn(Optional.of(entity));

        Optional<Cart> result = adapter.findByUserId(userId);

        assertTrue(result.isPresent());
        assertEquals(cart.getId(), result.get().getId());
        assertEquals(userId, result.get().getUserId());
        assertEquals(1, result.get().getItems().size());
        assertEquals(2, result.get().getItems().getFirst().getQuantity());
    }

    @Test
    void shouldSaveNewCart() {
        Cart cart = Cart.create(userId);
        UUID variantId = UUID.randomUUID();

        cart.addItem(variantId, 3);

        when(springDataCartRepository.findById(cart.getId()))
                .thenReturn(Optional.empty());

        when(springDataCartRepository.save(any(CartJpaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = adapter.save(cart);

        assertEquals(cart.getId(), result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(1, result.getItems().size());
        assertEquals(variantId, result.getItems().getFirst().getVariantId());
        assertEquals(3, result.getItems().getFirst().getQuantity());

        verify(springDataCartRepository).save(any(CartJpaEntity.class));
    }

    @Test
    void shouldUpdateExistingCartWithoutRecreatingItems() {
        Cart cart = Cart.create(userId);
        UUID variantId = UUID.randomUUID();

        cart.addItem(variantId, 2);

        CartJpaEntity entity = mapper.toEntity(cart);
        UUID originalItemId = entity.getItems().getFirst().getId();

        cart.changeItemQuantity(variantId, 5);

        when(springDataCartRepository.findById(cart.getId()))
                .thenReturn(Optional.of(entity));

        when(springDataCartRepository.save(entity))
                .thenReturn(entity);

        Cart result = adapter.save(cart);

        assertEquals(1, result.getItems().size());
        assertEquals(originalItemId, result.getItems().getFirst().getId());
        assertEquals(5, result.getItems().getFirst().getQuantity());
        assertEquals(cart.getUpdatedAt(), entity.getUpdatedAt());
    }

    @Test
    void shouldRemoveDeletedItems() {
        Cart cart = Cart.create(userId);

        UUID firstVariantId = UUID.randomUUID();
        UUID secondVariantId = UUID.randomUUID();

        cart.addItem(firstVariantId, 2);
        cart.addItem(secondVariantId, 1);

        CartJpaEntity entity = mapper.toEntity(cart);

        cart.removeItem(firstVariantId);

        when(springDataCartRepository.findById(cart.getId()))
                .thenReturn(Optional.of(entity));

        when(springDataCartRepository.save(entity))
                .thenReturn(entity);

        Cart result = adapter.save(cart);

        assertEquals(1, result.getItems().size());
        assertEquals(secondVariantId, result.getItems().getFirst().getVariantId());
        assertEquals(1, entity.getItems().size());
    }

    @Test
    void shouldAddNewItemToExistingCart() {
        Cart cart = Cart.create(userId);

        UUID firstVariantId = UUID.randomUUID();
        UUID secondVariantId = UUID.randomUUID();

        cart.addItem(firstVariantId, 2);

        CartJpaEntity entity = mapper.toEntity(cart);

        cart.addItem(secondVariantId, 4);

        when(springDataCartRepository.findById(cart.getId()))
                .thenReturn(Optional.of(entity));

        when(springDataCartRepository.save(entity))
                .thenReturn(entity);

        Cart result = adapter.save(cart);

        assertEquals(2, result.getItems().size());
        assertEquals(2, entity.getItems().size());

        assertTrue(result.getItems().stream()
                .anyMatch(item -> item.getVariantId().equals(secondVariantId)
                        && item.getQuantity() == 4));
    }

    @Test
    void shouldClearAllItems() {
        Cart cart = Cart.create(userId);

        cart.addItem(UUID.randomUUID(), 2);
        cart.addItem(UUID.randomUUID(), 3);

        CartJpaEntity entity = mapper.toEntity(cart);

        cart.clear();

        when(springDataCartRepository.findById(cart.getId()))
                .thenReturn(Optional.of(entity));

        when(springDataCartRepository.save(entity))
                .thenReturn(entity);

        Cart result = adapter.save(cart);

        assertTrue(result.getItems().isEmpty());
        assertTrue(entity.getItems().isEmpty());
    }
}
