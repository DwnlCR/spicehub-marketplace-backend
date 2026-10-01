package br.com.dwnl.spicehub.identity.application.exception;

public class InvalidPasswordResetCodeException extends RuntimeException {
    public InvalidPasswordResetCodeException() {
        super("Invalid or expired password reset code");
    }
}
