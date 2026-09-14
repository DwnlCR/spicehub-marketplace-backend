package br.com.dwnl.spicehub.identity.application.exception;

public class UserDisabledException extends RuntimeException {
    public UserDisabledException() {
        super("User account is disabled");
    }
}
