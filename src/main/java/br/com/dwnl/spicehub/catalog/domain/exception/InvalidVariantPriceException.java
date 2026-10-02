package br.com.dwnl.spicehub.catalog.domain.exception;

public class InvalidVariantPriceException extends RuntimeException {
    public InvalidVariantPriceException(String message) {
        super(message);
    }
}
