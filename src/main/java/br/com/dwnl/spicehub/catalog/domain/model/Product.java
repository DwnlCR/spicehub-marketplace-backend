package br.com.dwnl.spicehub.catalog.domain.model;

import br.com.dwnl.spicehub.catalog.domain.exception.InvalidProductNameException;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Getter
public class Product {

    private static final int MAX_NAME_LENGTH = 150;

    private final UUID id;
    private final Long version;
    private String name;
    private String description;
    private UUID categoryId;
    private String imageKey;
    private ProductStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Product(UUID id, Long version, String name, String description, UUID categoryId, String imageKey, ProductStatus status, Instant createdAt, Instant updatedAt) {
        this.id = requireNonNull(id, "Product id cannot be null");
        this.version = version;
        this.name = validateName(name);
        this.description = normalizeNullableText(description);
        this.categoryId = requireNonNull(categoryId, "Category id cannot be null");
        this.imageKey = normalizeNullableText(imageKey);
        this.status = requireNonNull(status, "Product status cannot be null");
        this.createdAt = requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    public static Product create(String name, String description, UUID categoryId, String imageKey){
        Instant now = Instant.now();

        return new Product(UUID.randomUUID(), null, name, description, categoryId, imageKey, ProductStatus.ACTIVE, now, now);
    }

    public static Product restore(UUID id, Long version, String name, String description, UUID categoryId, String imageKey, ProductStatus status, Instant createdAt, Instant updatedAt){

        return new Product(
                id,
                requireNonNull(version, "Product version cannot be null"),
                name, description, categoryId, imageKey, status, createdAt, updatedAt);
    }

    public void rename(String name){
        String normalizedName = validateName(name);

        if (!this.name.equals(normalizedName)){
            this.name = normalizedName;
            touch();
        }
    }

    public void changeDescription(String description) {
        String normalizedDescription = normalizeNullableText(description);

        if (!Objects.equals(this.description, normalizedDescription)) {
            this.description = normalizedDescription;
            touch();
        }
    }

    public void changeCategory(UUID categoryId){
        requireNonNull(categoryId, "Category id cannot be null");

        if (!this.categoryId.equals(categoryId)) {
            this.categoryId = categoryId;
            touch();
        }
    }

    public void changeImage(String imageKey){
        String normalizedImageKey = normalizeNullableText(imageKey);

        if (!Objects.equals(this.imageKey, normalizedImageKey)){
            this.imageKey = normalizedImageKey;
            touch();
        }
    }

    public void activate(){
        if (status != ProductStatus.ACTIVE){
            status = ProductStatus.ACTIVE;
            touch();
        }
    }

    public void deactivate(){
        if (status != ProductStatus.INACTIVE){
            status = ProductStatus.INACTIVE;
            touch();
        }
    }

    private void touch() {
        updatedAt = Instant.now();
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductNameException("Product name cannot be null or blank");
        }

        String normalized = name.trim();

        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new InvalidProductNameException("Product name exceeds the maximum length of " + MAX_NAME_LENGTH + " characters");
        }

        return normalized;
    }

    private static String normalizeNullableText(String value){
        if (value == null){
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty() ? null : normalized;
    }
}
