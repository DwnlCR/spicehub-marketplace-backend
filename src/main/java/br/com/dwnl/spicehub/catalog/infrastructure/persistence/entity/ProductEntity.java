package br.com.dwnl.spicehub.catalog.infrastructure.persistence.entity;

import br.com.dwnl.spicehub.catalog.domain.model.ProductStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor
public class ProductEntity {

    @Id
    private UUID id;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "image_key")
    private String imageKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProductEntity(UUID id, Long version,String name, String description, UUID categoryId, String imageKey, ProductStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.version = version;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.imageKey = imageKey;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
