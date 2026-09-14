package br.com.dwnl.spicehub.identity.infrastructure.persistence.adapter;

import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import br.com.dwnl.spicehub.identity.infrastructure.persistence.entity.UserEntity;
import br.com.dwnl.spicehub.identity.infrastructure.persistence.mapper.UserPersistenceMapper;
import br.com.dwnl.spicehub.identity.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository userRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);
        UserEntity savedEntity = userRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return userRepository.findByEmailIgnoreCase(email.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userRepository.existsByEmailIgnoreCase(email.value());
    }
}
