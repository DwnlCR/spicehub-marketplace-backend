package br.com.dwnl.spicehub.identity.application.port;

import br.com.dwnl.spicehub.identity.domain.model.Email;

public interface EmailVerificationCodeService {

    String create(Email email);

    boolean validateAndConsume(Email email, String code);

    boolean acquireResendCooldown(Email email);
}