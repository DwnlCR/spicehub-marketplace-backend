package br.com.dwnl.spicehub.identity.application.exception;

import java.util.UUID;

public class AuthenticatedUserNotFoundException extends RuntimeException {
    public AuthenticatedUserNotFoundException(UUID userId) {
        super("Authenticated user not found: " + userId);
    }
}
