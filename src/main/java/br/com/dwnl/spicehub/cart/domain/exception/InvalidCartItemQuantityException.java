package br.com.dwnl.spicehub.cart.domain.exception;

public class InvalidCartItemQuantityException extends RuntimeException {
    public InvalidCartItemQuantityException(String message) {
        super(message);
    }
}
