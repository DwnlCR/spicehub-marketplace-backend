package br.com.dwnl.spicehub.identity.infrastructure.persistence.entity;

import br.com.dwnl.spicehub.identity.domain.model.RoleName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "roles")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "name",
            nullable = false,
            unique = true,
            length = 50
    )
    private RoleName name;

    public RoleEntity(RoleName name){
        this.name = name;
    }
}
