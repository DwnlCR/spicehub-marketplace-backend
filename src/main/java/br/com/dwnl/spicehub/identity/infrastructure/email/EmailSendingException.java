package br.com.dwnl.spicehub.identity.infrastructure.email;

public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
