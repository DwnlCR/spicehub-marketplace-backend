package br.com.dwnl.spicehub.cart.infrastructure.persistence.adapter;

import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.domain.repository.CartRepository;
import br.com.dwnl.spicehub.cart.infrastructure.persistence.entity.CartItemJpaEntity;
import br.com.dwnl.spicehub.cart.infrastructure.persistence.entity.CartJpaEntity;
import br.com.dwnl.spicehub.cart.infrastructure.persistence.mapper.CartJpaMapper;
import br.com.dwnl.spicehub.cart.infrastructure.persistence.repository.SpringDataCartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaCartRepositoryAdapter implements CartRepository {

    private final SpringDataCartRepository springDataCartRepository;
    private final CartJpaMapper mapper;

    @Override
    public Optional<Cart> findByUserId(UUID userId){
        return springDataCartRepository.findByUserId(userId)
                .map(mapper::toDomain);
    }

    @Override
    public Cart save(Cart cart) {
        Optional<CartJpaEntity> existing = springDataCartRepository.findById(cart.getId());

        if (existing.isEmpty()){
            CartJpaEntity savedEntity = springDataCartRepository.save(mapper.toEntity(cart));

            return mapper.toDomain(savedEntity);
        }

        CartJpaEntity entity = existing.get();

        List<CartItemJpaEntity> updatedItems = cart.getItems().stream().map(item ->
                        new CartItemJpaEntity(
                                item.getId(),
                                item.getVariantId(),
                                item.getQuantity()
                        )
                ).toList();

        entity.synchronizeItems(updatedItems);
        entity.updateTimestamp(cart.getUpdatedAt());

        CartJpaEntity savedEntity = springDataCartRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }
}
