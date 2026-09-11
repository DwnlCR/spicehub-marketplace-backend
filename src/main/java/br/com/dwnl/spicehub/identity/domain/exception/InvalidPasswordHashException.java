package br.com.dwnl.spicehub.identity.domain.exception;

public class InvalidPasswordHashException extends IdentityDomainException {
    public InvalidPasswordHashException(String message) {
        super(message);
    }
}
