package br.com.dwnl.spicehub.catalog.domain.exception;

public class InvalidProductNameException extends RuntimeException {
    public InvalidProductNameException(String message) {
        super(message);
    }
}
