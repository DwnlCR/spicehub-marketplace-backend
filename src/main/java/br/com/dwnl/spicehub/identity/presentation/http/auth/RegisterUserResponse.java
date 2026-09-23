package br.com.dwnl.spicehub.identity.presentation.http.auth;

import br.com.dwnl.spicehub.identity.application.result.RegisterUserResult;
import br.com.dwnl.spicehub.identity.domain.model.RoleName;
import br.com.dwnl.spicehub.identity.domain.model.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RegisterUserResponse(
        UUID id,
        String name,
        String email,
        Set<RoleName> roles,
        Instant createdAt,
        boolean verificationEmailSent
) {

    public static RegisterUserResponse from(RegisterUserResult result) {
        User user = result.user();

        return new RegisterUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail().value(),
                user.getRoles(),
                user.getCreatedAt(),
                result.verificationEmailSent()
        );
    }
}