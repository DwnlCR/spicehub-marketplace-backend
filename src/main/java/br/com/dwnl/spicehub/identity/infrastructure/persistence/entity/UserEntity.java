package br.com.dwnl.spicehub.identity.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    @Id
    @Column(name = "id",
            nullable = false,
            updatable = false
    )
    private UUID id;

    @Column(name = "name",
            nullable = false,
            length = 120
    )
    private String name;

    @Column(name = "email",
            nullable = false,
            length = 320
    )
    private String email;

    @Column(name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;

    @Column(name = "enabled",
            nullable = false
    )
    private boolean enabled;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "role_id", nullable = false)
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @Column(name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    public UserEntity(UUID id, String name, String email, String passwordHash, boolean enabled, Set<RoleEntity> roles, Instant createdAt, Instant updatedAt){
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.roles = new HashSet<>(roles);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
