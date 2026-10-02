package br.com.dwnl.spicehub.catalog.domain.exception;

public class InvalidVariantQuantityException extends RuntimeException {
    public InvalidVariantQuantityException(String message) {
        super(message);
    }
}
