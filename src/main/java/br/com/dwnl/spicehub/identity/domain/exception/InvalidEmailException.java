package br.com.dwnl.spicehub.identity.domain.exception;

public class InvalidEmailException extends IdentityDomainException {
    public InvalidEmailException(String message) {
        super(message);
    }
}
