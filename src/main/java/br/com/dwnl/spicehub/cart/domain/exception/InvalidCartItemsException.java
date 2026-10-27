package br.com.dwnl.spicehub.cart.domain.exception;

public class InvalidCartItemsException extends RuntimeException {
    public InvalidCartItemsException(String message) {
        super(message);
    }
}
