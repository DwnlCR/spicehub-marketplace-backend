package br.com.dwnl.spicehub.identity.infrastructure.security.exception;

public class CryptographicOperationException extends RuntimeException {
    public CryptographicOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
