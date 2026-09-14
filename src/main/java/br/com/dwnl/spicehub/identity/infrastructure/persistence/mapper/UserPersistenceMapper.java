package br.com.dwnl.spicehub.identity.infrastructure.persistence.mapper;

import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.RoleName;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.infrastructure.persistence.entity.RoleEntity;
import br.com.dwnl.spicehub.identity.infrastructure.persistence.entity.UserEntity;
import br.com.dwnl.spicehub.identity.infrastructure.persistence.exception.RoleNotFoundException;
import br.com.dwnl.spicehub.identity.infrastructure.persistence.repository.SpringDataRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPersistenceMapper {
    private final SpringDataRoleRepository roleRepository;

    public User toDomain(UserEntity entity){
        Set<RoleName> roles = entity.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());

        return User.restore(
                entity.getId(),
                entity.getName(),
                new Email(entity.getEmail()),
                entity.getPasswordHash(),
                entity.isEnabled(),
                roles,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public UserEntity toEntity(User user){
        Set<RoleEntity> roles = user.getRoles().stream()
                .map(this::findRoleEntity)
                .collect(Collectors.toSet());

        return new UserEntity(
                user.getId(),
                user.getName(),
                user.getEmail().value(),
                user.getPasswordHash(),
                user.isEnabled(),
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private RoleEntity findRoleEntity(RoleName roleName){
        return roleRepository.findByName(roleName).orElseThrow(
                () -> new RoleNotFoundException(roleName)
        );
    }
}
