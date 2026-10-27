package br.com.dwnl.spicehub.cart.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_items_cart_variant",
                columnNames = {"cart_id", "variant_id"}
        )
    )
@NoArgsConstructor
public class CartItemJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartJpaEntity cart;

    @Column(name = "variant_id", nullable = false)
    private UUID variantId;

    @Column(nullable = false)
    private int quantity;

    public CartItemJpaEntity(UUID id, UUID variantId, int quantity){
        this.id = id;
        this.variantId = variantId;
        this.quantity = quantity;
    }

    void setCart(CartJpaEntity cart){
        this.cart = cart;
    }

    public void changeQuantity(int quantity){
        this.quantity = quantity;
    }
}
