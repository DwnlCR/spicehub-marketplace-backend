package br.com.dwnl.spicehub.catalog.application.exception;

public class ProductVariantAlreadyExistsException extends RuntimeException {
    public ProductVariantAlreadyExistsException(String message) {
        super(message);
    }
}
