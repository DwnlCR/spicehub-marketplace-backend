package br.com.dwnl.spicehub.identity.domain.model;

import br.com.dwnl.spicehub.identity.domain.exception.DefaultRoleRemovalException;
import br.com.dwnl.spicehub.identity.domain.exception.InvalidPasswordHashException;
import br.com.dwnl.spicehub.identity.domain.exception.InvalidUserNameException;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Instant;
import java.util.*;

@Getter
public class User {

    private static final int MAX_NAME_LENGTH = 120;

    private final UUID id;

    private String name;

    private Email email;

    private String passwordHash;

    private boolean enabled;

    @Getter(AccessLevel.NONE)
    private final Set<RoleName> roles;

    private final Instant createdAt;

    private Instant updatedAt;

    private User(UUID id, String name, Email email, String passwordHash,
                 boolean enabled, Set<RoleName> roles, Instant createdAt, Instant updatedAt){

        this.id = Objects.requireNonNull(id, "User id cannot be null");
        this.name = validateName(name);
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.passwordHash = validatePasswordHash(passwordHash);
        this.enabled = enabled;
        this.roles = new HashSet<>(Objects.requireNonNull(roles, "Roles cannot be null"));
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");

    }

    public static User create(String name, Email email, String passwordHash){
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), name, email, passwordHash, true,
                Set.of(RoleName.USER), now, now);
    }

    public static User restore(UUID id, String name, Email email, String passwordHash, boolean enabled,
                               Set<RoleName> roles, Instant createdAt, Instant updatedAt){
        return new User(id, name, email, passwordHash, enabled, roles, createdAt, updatedAt);
    }

    public void grantRole(RoleName role){
        Objects.requireNonNull(role, "Role cannot be null");
        touch();
    }

    public void revokeRole(RoleName role){
        Objects.requireNonNull(role, "Role cannot be null");

        if (role == RoleName.USER){
            throw new DefaultRoleRemovalException();
        }

        if (roles.remove(role)){
            touch();
        }
    }

    public void enable() {
        if (!enabled) {
            enabled = true;
            touch();
        }
    }

    public void disable() {
        if (enabled) {
            enabled = false;
            touch();
        }
    }

    private void touch() {
        updatedAt = Instant.now();
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidUserNameException(
                    "User name cannot be null or blank"
            );
        }

        String normalized = name.trim();

        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new InvalidUserNameException(
                    "User name exceeds the maximum length of "
                            + MAX_NAME_LENGTH
                            + " characters"
            );
        }

        return normalized;
    }

    private static String validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidPasswordHashException(
                    "Password hash cannot be null or blank"
            );
        }

        return passwordHash;
    }

    public Set<RoleName> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
}
