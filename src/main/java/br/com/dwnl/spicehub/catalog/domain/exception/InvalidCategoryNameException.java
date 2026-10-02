package br.com.dwnl.spicehub.catalog.domain.exception;

public class InvalidCategoryNameException extends RuntimeException {
    public InvalidCategoryNameException(String message) {
        super(message);
    }
}
