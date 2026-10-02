package br.com.dwnl.spicehub.catalog.domain.model;

import br.com.dwnl.spicehub.catalog.domain.exception.InvalidVariantPriceException;
import br.com.dwnl.spicehub.catalog.domain.exception.InvalidVariantQuantityException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Getter
public class ProductVariant {

    private final UUID id;
    private final UUID productId;

    private int quantity;
    private MeasurementUnit measurementUnit;
    private BigDecimal price;
    private VariantAvailability availability;

    private final Instant createdAt;
    private Instant updatedAt;

    private ProductVariant(UUID id, UUID productId, int quantity, MeasurementUnit measurementUnit, BigDecimal price, VariantAvailability availability, Instant createdAt, Instant updatedAt) {
        this.id = requireNonNull(id, "Product variant id cannot be null");
        this.productId = requireNonNull(productId, "Product id cannot be null");
        this.quantity = validateQuantity(quantity);
        this.measurementUnit = requireNonNull(measurementUnit, "Measurement unit cannot be null");
        this.price = validatePrice(price);
        this.availability = requireNonNull(availability, "Variant availability cannot be null");
        this.createdAt = requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    public static ProductVariant create(UUID productId, int quantity, MeasurementUnit measurementUnit, BigDecimal price){
        Instant now = Instant.now();

        return new ProductVariant(UUID.randomUUID(), productId, quantity, measurementUnit, price, VariantAvailability.AVAILABLE, now, now);
    }

    public static ProductVariant restore(UUID id, UUID productId, int quantity, MeasurementUnit measurementUnit, BigDecimal price, VariantAvailability availability, Instant createdAt, Instant updatedAt){

        return new ProductVariant(id, productId, quantity, measurementUnit, price, availability, createdAt, updatedAt);
    }

    public void changeMeasurement(int quantity, MeasurementUnit measurementUnit){
        int validateQuantity = validateQuantity(quantity);
        MeasurementUnit validateUnit = requireNonNull(measurementUnit, "Measurement unit cannot be null");

        if (this.quantity != validateQuantity || this.measurementUnit != validateUnit){
            this.quantity = validateQuantity;
            this.measurementUnit =  validateUnit;
            touch();
        }
    }

    public void changePrice(BigDecimal price){
        BigDecimal validatedPrice = validatePrice(price);

        if (this.price.compareTo(validatedPrice) != 0){
            this.price = validatedPrice;
            touch();
        }
    }

    public void markAvailable(){
        if (availability != VariantAvailability.AVAILABLE){
            availability = VariantAvailability.AVAILABLE;
            touch();
        }
    }


    public void markSoldOut(){
        if (availability != VariantAvailability.SOLD_OUT){
            availability = VariantAvailability.SOLD_OUT;
            touch();
        }
    }

    private void touch(){
        updatedAt = Instant.now();
    }

    private static int validateQuantity(int quantity){
        if (quantity <= 0){
            throw new InvalidVariantQuantityException("Variant quantity must be greater than zero");
        }
        return quantity;
    }

    private static BigDecimal validatePrice(BigDecimal price){
        if (price == null){
            throw new InvalidVariantPriceException("Variant price cannot be null");
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidVariantPriceException("Variant price must be greater than zero");
        }

        return price;
    }
}
