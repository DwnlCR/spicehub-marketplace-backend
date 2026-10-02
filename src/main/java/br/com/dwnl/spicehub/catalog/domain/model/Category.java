package br.com.dwnl.spicehub.catalog.domain.model;

import br.com.dwnl.spicehub.catalog.domain.exception.InvalidCategoryNameException;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Getter
public class Category {
    private static final int MAX_NAME_LENGTH = 100;

    private final UUID id;
    private String name;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private Category(UUID id, String name, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = requireNonNull(id, "Category id cannot be null");
        this.name = validateName(name);
        this.active = active;
        this.createdAt = requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    public static Category create(String name){
        Instant now = Instant.now();

        return new Category(UUID.randomUUID(), name, true, now, now);
    }

    public static Category restore(UUID id, String name, boolean active, Instant createdAt, Instant updatedAt){

        return new Category(id, name, active, createdAt, updatedAt);
    }

    public void rename(String name){
        String normalizedName = validateName(name);

        if (!this.name.equals(normalizedName)){
            this.name = normalizedName;
            touch();
        }
    }

    public void activate(){
        if (!active){
            active = true;
            touch();
        }
    }

    public void deactivate(){
        if (active){
            active = false;
            touch();
        }
    }

    private void touch(){
        updatedAt = Instant.now();
    }

    private static String validateName(String name){
        if (name == null || name.isBlank()){
            throw new InvalidCategoryNameException("Category name cannot be null or blank");
        }

        String normalized = name.trim();

        if (normalized.length() > MAX_NAME_LENGTH){
            throw new InvalidCategoryNameException("Category name exceeds the maximum length of " + MAX_NAME_LENGTH + " characters");
        }

        return normalized;
    }
}
