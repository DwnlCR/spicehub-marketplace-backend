package br.com.dwnl.spicehub.cart.application.exception;

public class CartVariantNotFoundException extends RuntimeException {
    public CartVariantNotFoundException() {
        super("Product variant not found");
    }
}
