package br.com.dwnl.spicehub.identity.domain.repository;

import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(Email email);
    boolean existsByEmail(Email email);

}
