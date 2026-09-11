package br.com.dwnl.spicehub.identity.domain.exception;

public class InvalidUserNameException extends IdentityDomainException {
    public InvalidUserNameException(String message) {
        super(message);
    }
}
