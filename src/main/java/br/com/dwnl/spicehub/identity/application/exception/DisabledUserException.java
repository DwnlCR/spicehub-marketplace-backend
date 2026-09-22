package br.com.dwnl.spicehub.identity.application.exception;

public class DisabledUserException extends RuntimeException {
    public DisabledUserException() {
        super("User account is disabled");
    }
}
