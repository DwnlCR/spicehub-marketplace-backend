package br.com.dwnl.spicehub.cart.infrastructure.persistence.mapper;

import br.com.dwnl.spicehub.cart.domain.model.Cart;
import br.com.dwnl.spicehub.cart.domain.model.CartItem;
import br.com.dwnl.spicehub.cart.infrastructure.persistence.entity.CartItemJpaEntity;
import br.com.dwnl.spicehub.cart.infrastructure.persistence.entity.CartJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartJpaMapper {
    public Cart toDomain(CartJpaEntity entity){
        List<CartItem> items = entity.getItems().stream()
                .map(item -> CartItem.restore(
                        item.getId(),
                        item.getVariantId(),
                        item.getQuantity()
                ))
                .toList();

        return Cart.restore(
                entity.getId(),
                entity.getUserId(),
                items,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public CartJpaEntity toEntity(Cart cart){
        CartJpaEntity entity = new CartJpaEntity(
                cart.getId(),
                null,
                cart.getUserId(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );

        cart.getItems().forEach(
                item -> entity.addItem(new CartItemJpaEntity(
                        item.getId(),
                        item.getVariantId(),
                        item.getQuantity()
                ))
        );

        return entity;
    }
}
