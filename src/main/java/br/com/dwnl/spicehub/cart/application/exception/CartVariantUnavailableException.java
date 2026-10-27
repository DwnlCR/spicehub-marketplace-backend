package br.com.dwnl.spicehub.cart.application.exception;

public class CartVariantUnavailableException extends RuntimeException {
    public CartVariantUnavailableException() {
        super("Product variant is unavailable for purchase");
    }
}
