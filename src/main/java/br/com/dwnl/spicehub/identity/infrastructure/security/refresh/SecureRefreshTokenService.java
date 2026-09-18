package br.com.dwnl.spicehub.identity.infrastructure.security.refresh;

import br.com.dwnl.spicehub.identity.application.exception.InvalidRefreshTokenException;
import br.com.dwnl.spicehub.identity.application.model.RefreshSession;
import br.com.dwnl.spicehub.identity.application.model.RefreshToken;
import br.com.dwnl.spicehub.identity.application.port.RefreshSessionRepository;
import br.com.dwnl.spicehub.identity.application.port.RefreshTokenService;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.infrastructure.security.exception.CryptographicOperationException;
import br.com.dwnl.spicehub.identity.infrastructure.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecureRefreshTokenService implements RefreshTokenService {

    private static final int TOKEN_BYTES = 32;

    private final RefreshSessionRepository refreshSessionRepository;
    private final JwtProperties jwtProperties;

    @Override
    public RefreshToken generate(User user) {
        UUID sessionId = UUID.randomUUID();

        String secret = generateSecret();
        String tokenHash = hash(secret);

        Instant createdAt = Instant.now();
        Instant expiredAt = createdAt.plus(jwtProperties.refreshTokenTtl());

        RefreshSession session = new RefreshSession(
                sessionId,
                user.getId(),
                tokenHash,
                createdAt,
                expiredAt
        );

        refreshSessionRepository.save(session);

        String tokenValue = sessionId + "." + secret;

        return new RefreshToken(tokenValue, expiredAt);
    }

    @Override
    public UUID validateAndConsume(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        String[] parts = refreshToken.split("\\.", 2);

        if (parts.length != 2 || parts[1].isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        UUID sessionId;

        try {
            sessionId = UUID.fromString(parts[0]);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRefreshTokenException();
        }

        String secret = parts[1];
        String receivedHash = hash(secret);

        RefreshSession session = refreshSessionRepository
                .consumeIfMatches(sessionId, receivedHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (session.expiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }

        return session.userId();
    }

    @Override
    public void revoke(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()){
            return;
        }

        String [] parts = refreshToken.split("\\.", 2);

        if (parts.length != 2){
            return;
        }

        UUID sessionId;

        try {
            sessionId = UUID.fromString(parts[0]);
        }
        catch (IllegalArgumentException exception){
            return;
        }

        refreshSessionRepository.deleteById(sessionId);
    }

    private String generateSecret(){
        byte[] bytes = new byte[TOKEN_BYTES];

        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hash(String value){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new CryptographicOperationException(
                    "Failed to hash refresh token",
                    exception
            );
        }
    }
}
