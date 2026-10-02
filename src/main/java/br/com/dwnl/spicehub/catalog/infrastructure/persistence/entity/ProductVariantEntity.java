package br.com.dwnl.spicehub.catalog.infrastructure.persistence.entity;

import br.com.dwnl.spicehub.catalog.domain.model.MeasurementUnit;
import br.com.dwnl.spicehub.catalog.domain.model.VariantAvailability;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "product_variants")
@NoArgsConstructor
public class ProductVariantEntity {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_unit", nullable = false, length = 20)
    private MeasurementUnit measurementUnit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VariantAvailability availability;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProductVariantEntity(UUID id, UUID productId, int quantity, MeasurementUnit measurementUnit, BigDecimal price, VariantAvailability availability, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.measurementUnit = measurementUnit;
        this.price = price;
        this.availability = availability;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
