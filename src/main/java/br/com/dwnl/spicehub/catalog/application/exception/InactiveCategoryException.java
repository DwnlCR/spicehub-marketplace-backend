package br.com.dwnl.spicehub.catalog.application.exception;

public class InactiveCategoryException extends RuntimeException {
    public InactiveCategoryException(String message) {
        super(message);
    }
}
