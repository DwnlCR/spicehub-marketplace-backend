package br.com.dwnl.spicehub.identity.infrastructure.security.cookie;

import br.com.dwnl.spicehub.identity.application.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieService {

    private static final String COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/auth";

    private final RefreshCookieProperties properties;

    public ResponseCookie create(RefreshToken refreshToken){
        Duration maxAge = Duration.between(Instant.now(), refreshToken.expiredAt());

        return ResponseCookie.from(COOKIE_NAME, refreshToken.value())
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie clear(){
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path(COOKIE_PATH)
                .maxAge(Duration.ZERO)
                .build();
    }
}
