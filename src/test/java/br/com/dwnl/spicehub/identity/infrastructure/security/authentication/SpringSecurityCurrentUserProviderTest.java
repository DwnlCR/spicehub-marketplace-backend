package br.com.dwnl.spicehub.identity.infrastructure.security.authentication;

import br.com.dwnl.spicehub.identity.application.model.AuthenticatedUser;
import br.com.dwnl.spicehub.identity.domain.model.RoleName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringSecurityCurrentUserProviderTest {

    private final SpringSecurityCurrentUserProvider provider =
            new SpringSecurityCurrentUserProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnCurrentUserFromJwt() {
        UUID userId = UUID.randomUUID();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .claim("email", "user@gmail.com")
                .claim("roles", List.of("USER"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        jwt,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        AuthenticatedUser result = provider.getCurrentUser();

        assertEquals(userId, result.id());
        assertEquals("user@gmail.com", result.email());
        assertEquals(
                java.util.Set.of(RoleName.USER),
                result.roles()
        );
    }
}