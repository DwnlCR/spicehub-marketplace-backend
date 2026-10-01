package br.com.dwnl.spicehub.identity.application.exception;

public class TooManyRegistrationAttemptsException extends RuntimeException {
    public TooManyRegistrationAttemptsException() {
        super("Too many registration attempts. Try again later.");
    }
}
