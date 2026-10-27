package br.com.dwnl.spicehub.cart.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.*;

@Getter
@Entity
@Table(
        name = "carts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_carts_user_id",
                columnNames = "user_id"
        )
    )
@NoArgsConstructor
public class CartJpaEntity {

    @Id
    private UUID id;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CartItemJpaEntity> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CartJpaEntity(UUID id, Long version, UUID userId, Instant createdAt, Instant updatedAt){
        this.id = id;
        this.version = version;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addItem(CartItemJpaEntity item){
        items.add(item);
        item.setCart(this);
    }

    public void clearItems(){
        for (CartItemJpaEntity item : items){
            item.setCart(null);
        }
        items.clear();
    }


    public void updateTimestamp(Instant updatedAt){
        this.updatedAt = updatedAt;
    }

    public void synchronizeItems(List<CartItemJpaEntity> updatedItems){
        Set<UUID> updatedIds = new HashSet<>();

        for (CartItemJpaEntity updatedItem : updatedItems){
            updatedIds.add(updatedItem.getId());
        }

        for (var iterator = items.iterator(); iterator.hasNext();){
            CartItemJpaEntity existingItem = iterator.next();

            if (!updatedIds.contains(existingItem.getId())){
                iterator.remove();
                existingItem.setCart(null);
            }
        }

        for (CartItemJpaEntity updatedItem : updatedItems){
            CartItemJpaEntity existingItem = items.stream()
                    .filter(item -> item.getId().equals(updatedItem.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingItem == null){
                addItem(updatedItem);
            }
            else {
                existingItem.changeQuantity(updatedItem.getQuantity());
            }
        }
    }
}
