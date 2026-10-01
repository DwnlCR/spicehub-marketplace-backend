package br.com.dwnl.spicehub.identity.infrastructure.security.resetpassword;

import br.com.dwnl.spicehub.identity.domain.model.Email;

import java.time.Duration;

public interface PasswordResetCodeRepository {

    void save(Email email, String codeHash, Duration ttl);

    boolean consumeIfMatches(Email email, String codeHash, int maxAttempts);

    boolean acquireCooldown(Email email, Duration ttl);

    void deleteCodeAndCooldown(Email email);
}