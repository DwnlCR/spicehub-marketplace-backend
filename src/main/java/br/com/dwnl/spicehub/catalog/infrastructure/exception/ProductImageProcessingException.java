
package br.com.dwnl.spicehub.catalog.infrastructure.exception;

public class ProductImageProcessingException extends RuntimeException {

    public ProductImageProcessingException(String message) {
        super(message);
    }

    public ProductImageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
