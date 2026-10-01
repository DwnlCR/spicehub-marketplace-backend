package br.com.dwnl.spicehub.identity.infrastructure.security.resetpassword;

import br.com.dwnl.spicehub.config.PostgresTestContainerConfig;
import br.com.dwnl.spicehub.config.RedisTestContainerConfig;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({
        PostgresTestContainerConfig.class,
        RedisTestContainerConfig.class
})
class RedisPasswordResetCodeRepositoryTest {

    @Autowired
    private RedisPasswordResetCodeRepository repository;

    @Test
    void shouldConsumeValidCode() {
        Email email = new Email("reset-valid@gmail.com");

        repository.save(
                email,
                "correct-hash",
                Duration.ofMinutes(1)
        );

        boolean result = repository.consumeIfMatches(
                email,
                "correct-hash",
                5
        );

        assertTrue(result);

        boolean secondAttempt = repository.consumeIfMatches(
                email,
                "correct-hash",
                5
        );

        assertFalse(secondAttempt);
    }

    @Test
    void shouldBlockCodeAfterMaximumFailedAttempts() {
        Email email = new Email("reset-attempts@gmail.com");

        repository.save(
                email,
                "correct-hash",
                Duration.ofMinutes(1)
        );

        for (int i = 0; i < 5; i++) {
            assertFalse(repository.consumeIfMatches(
                    email,
                    "wrong-hash",
                    5
            ));
        }

        boolean correctCodeAfterLimit = repository.consumeIfMatches(
                email,
                "correct-hash",
                5
        );

        assertFalse(correctCodeAfterLimit);
    }

    @Test
    void shouldResetAttemptsWhenNewCodeIsSaved() {
        Email email = new Email("reset-new-code@gmail.com");

        repository.save(
                email,
                "first-hash",
                Duration.ofMinutes(1)
        );

        for (int i = 0; i < 5; i++) {
            repository.consumeIfMatches(
                    email,
                    "wrong-hash",
                    5
            );
        }

        repository.save(
                email,
                "second-hash",
                Duration.ofMinutes(1)
        );

        boolean result = repository.consumeIfMatches(
                email,
                "second-hash",
                5
        );

        assertTrue(result);
    }

    @Test
    void shouldDeleteCodeAndAllowCooldownAgainWhenInvalidated() {
        Email email = new Email("reset-invalidate@gmail.com");

        repository.save(
                email,
                "correct-hash",
                Duration.ofMinutes(1)
        );

        assertTrue(repository.acquireCooldown(
                email,
                Duration.ofMinutes(1)
        ));

        repository.consumeIfMatches(
                email,
                "wrong-hash",
                5
        );

        repository.deleteCodeAndCooldown(email);

        assertFalse(repository.consumeIfMatches(
                email,
                "correct-hash",
                5
        ));

        assertTrue(repository.acquireCooldown(
                email,
                Duration.ofMinutes(1)
        ));
    }
}