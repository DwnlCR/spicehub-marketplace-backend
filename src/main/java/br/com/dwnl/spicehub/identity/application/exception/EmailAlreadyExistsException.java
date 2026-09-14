package br.com.dwnl.spicehub.identity.application.exception;

import br.com.dwnl.spicehub.identity.domain.model.Email;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(Email email) {
        super("Email is already registered: " + email.value());
    }
}
