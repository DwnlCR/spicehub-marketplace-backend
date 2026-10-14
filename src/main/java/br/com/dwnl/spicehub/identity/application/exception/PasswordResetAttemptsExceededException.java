package br.com.dwnl.spicehub.identity.application.exception;

public class PasswordResetAttemptsExceededException extends RuntimeException {
    public PasswordResetAttemptsExceededException() {
        super("Password reset code attempts exceeded. Request a new code");
    }
}
