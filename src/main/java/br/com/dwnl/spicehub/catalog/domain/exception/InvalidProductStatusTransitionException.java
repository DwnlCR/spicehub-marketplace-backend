package br.com.dwnl.spicehub.catalog.domain.exception;

public class InvalidProductStatusTransitionException extends RuntimeException {
    public InvalidProductStatusTransitionException(String message) {
        super(message);
    }
}
