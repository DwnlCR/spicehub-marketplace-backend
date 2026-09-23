package br.com.dwnl.spicehub.identity.application.port;

import br.com.dwnl.spicehub.identity.domain.model.Email;

import java.time.Duration;

public interface EmailVerificationCodeRepository {

    void save(Email email, String codeHash, Duration codeTtl);

    boolean consumeIfMatches(Email email, String codeHash);

    boolean acquireResendCooldown(Email email, Duration ttl);
}
