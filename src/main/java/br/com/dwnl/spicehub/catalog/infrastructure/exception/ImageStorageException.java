package br.com.dwnl.spicehub.catalog.infrastructure.exception;

public class ImageStorageException extends RuntimeException {
    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
