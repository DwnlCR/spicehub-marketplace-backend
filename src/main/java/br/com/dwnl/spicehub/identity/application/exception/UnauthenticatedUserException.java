package br.com.dwnl.spicehub.identity.application.exception;

public class UnauthenticatedUserException extends RuntimeException {
    public UnauthenticatedUserException() {
        super("Authenticated user not found");
    }
}
