package br.com.dwnl.spicehub.identity.infrastructure.security.refresh;

import br.com.dwnl.spicehub.config.PostgresTestContainerConfig;
import br.com.dwnl.spicehub.config.RedisTestContainerConfig;
import br.com.dwnl.spicehub.identity.application.model.RefreshSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({
        PostgresTestContainerConfig.class,
        RedisTestContainerConfig.class
})
class RedisRefreshSessionRepositoryTest {

    @Autowired
    private RedisRefreshSessionRepository repository;

    @Test
    void shouldDeleteAllSessionsFromUserWithoutDeletingOtherUserSessions() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(3600);

        RefreshSession firstSession = new RefreshSession(
                UUID.randomUUID(),
                userId,
                "first-token-hash",
                createdAt,
                expiresAt
        );

        RefreshSession secondSession = new RefreshSession(
                UUID.randomUUID(),
                userId,
                "second-token-hash",
                createdAt,
                expiresAt
        );

        RefreshSession otherUserSession = new RefreshSession(
                UUID.randomUUID(),
                otherUserId,
                "other-token-hash",
                createdAt,
                expiresAt
        );

        repository.save(firstSession);
        repository.save(secondSession);
        repository.save(otherUserSession);

        repository.deleteAllByUserId(userId);

        assertTrue(repository.findById(firstSession.id()).isEmpty());
        assertTrue(repository.findById(secondSession.id()).isEmpty());

        assertTrue(repository.findById(otherUserSession.id()).isPresent());
    }
}