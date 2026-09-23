package br.com.dwnl.spicehub.identity.application.exception;

public class InvalidEmailVerificationCodeException extends RuntimeException {
    public InvalidEmailVerificationCodeException() {
        super("Invalid or expired email verification code");
    }
}
