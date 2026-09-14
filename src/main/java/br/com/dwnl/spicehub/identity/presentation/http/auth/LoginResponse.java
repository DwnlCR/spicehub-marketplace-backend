package br.com.dwnl.spicehub.identity.presentation.http.auth;

import br.com.dwnl.spicehub.identity.application.model.LoginResult;
import br.com.dwnl.spicehub.identity.domain.model.RoleName;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record LoginResponse(String accessToken, String tokenType, Instant expiresAt, UserInfo user) {

    public static LoginResponse from(LoginResult result){
        return new LoginResponse(
                result.accessToken().value(),
                "Bearer",
                result.accessToken().expiredAt(),
                new UserInfo(
                        result.user().getId(),
                        result.user().getName(),
                        result.user().getEmail().value(),
                        result.user().getRoles()
                )
        );
    }


    public record UserInfo(UUID id, String name, String email, Set<RoleName> roles){
    }
}
