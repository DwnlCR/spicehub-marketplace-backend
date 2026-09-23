package br.com.dwnl.spicehub.identity.application.port;

import br.com.dwnl.spicehub.identity.domain.model.Email;

public interface EmailVerificationEmailSender {

    void send(Email email, String code);
}
