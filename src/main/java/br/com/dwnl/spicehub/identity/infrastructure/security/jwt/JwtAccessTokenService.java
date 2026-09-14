package br.com.dwnl.spicehub.identity.infrastructure.security.jwt;

import br.com.dwnl.spicehub.identity.application.model.AccessToken;
import br.com.dwnl.spicehub.identity.application.port.AccessTokenService;
import br.com.dwnl.spicehub.identity.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAccessTokenService implements AccessTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    @Override
    public AccessToken generate(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(
                properties.accessTokenTtl()
        );

        List<String> roles = user.getRoles()
                .stream()
                .map(Enum::name)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("email", user.getEmail().value())
                .claim("roles", roles)
                .build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();

        return new AccessToken(
                token,
                expiresAt
        );
    }
}