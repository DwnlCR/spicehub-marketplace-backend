package br.com.dwnl.spicehub.catalog.infrastructure.exception;

public class InvalidProductImageException extends RuntimeException {
    public InvalidProductImageException(String message) {
        super(message);
    }
}
