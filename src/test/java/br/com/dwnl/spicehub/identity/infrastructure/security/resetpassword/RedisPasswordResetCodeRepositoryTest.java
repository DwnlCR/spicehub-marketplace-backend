
package br.com.dwnl.spicehub.identity.infrastructure.security.resetpassword;

import br.com.dwnl.spicehub.config.PostgresTestContainerConfig;
import br.com.dwnl.spicehub.config.RedisTestContainerConfig;
import br.com.dwnl.spicehub.identity.application.port.PasswordResetCodeValidationResult;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;

import static br.com.dwnl.spicehub.identity.application.port.PasswordResetCodeValidationResult.*;
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

        PasswordResetCodeValidationResult result =
                repository.consumeIfMatches(
                        email,
                        "correct-hash",
                        5
                );

        assertEquals(VALID, result);

        PasswordResetCodeValidationResult secondAttempt =
                repository.consumeIfMatches(
                        email,
                        "correct-hash",
                        5
                );

        assertEquals(INVALID, secondAttempt);
    }

    @Test
    void shouldBlockCodeAfterMaximumFailedAttempts() {
        Email email = new Email("reset-attempts@gmail.com");

        repository.save(
                email,
                "correct-hash",
                Duration.ofMinutes(1)
        );

        for (int i = 1; i < 5; i++) {
            PasswordResetCodeValidationResult result =
                    repository.consumeIfMatches(
                            email,
                            "wrong-hash",
                            5
                    );

            assertEquals(INVALID, result);
        }

        PasswordResetCodeValidationResult fifthAttempt =
                repository.consumeIfMatches(
                        email,
                        "wrong-hash",
                        5
                );

        assertEquals(ATTEMPTS_EXCEEDED, fifthAttempt);

        PasswordResetCodeValidationResult sixthAttempt =
                repository.consumeIfMatches(
                        email,
                        "wrong-hash",
                        5
                );

        assertEquals(ATTEMPTS_EXCEEDED, sixthAttempt);

        PasswordResetCodeValidationResult correctCodeAfterLimit =
                repository.consumeIfMatches(
                        email,
                        "correct-hash",
                        5
                );

        assertEquals(ATTEMPTS_EXCEEDED, correctCodeAfterLimit);
    }

    @Test
    void shouldAllowValidCodeBeforeMaximumFailedAttempts() {
        Email email = new Email("reset-before-limit@gmail.com");

        repository.save(
                email,
                "correct-hash",
                Duration.ofMinutes(1)
        );

        for (int i = 0; i < 4; i++) {
            assertEquals(
                    INVALID,
                    repository.consumeIfMatches(
                            email,
                            "wrong-hash",
                            5
                    )
            );
        }

        assertEquals(
                VALID,
                repository.consumeIfMatches(
                        email,
                        "correct-hash",
                        5
                )
        );

        assertEquals(
                INVALID,
                repository.consumeIfMatches(
                        email,
                        "correct-hash",
                        5
                )
        );
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

        assertEquals(
                ATTEMPTS_EXCEEDED,
                repository.consumeIfMatches(
                        email,
                        "first-hash",
                        5
                )
        );

        repository.save(
                email,
                "second-hash",
                Duration.ofMinutes(1)
        );

        assertEquals(
                VALID,
                repository.consumeIfMatches(
                        email,
                        "second-hash",
                        5
                )
        );
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

        assertEquals(
                INVALID,
                repository.consumeIfMatches(
                        email,
                        "wrong-hash",
                        5
                )
        );

        repository.deleteCodeAndCooldown(email);

        assertEquals(
                INVALID,
                repository.consumeIfMatches(
                        email,
                        "correct-hash",
                        5
                )
        );

        assertTrue(repository.acquireCooldown(
                email,
                Duration.ofMinutes(1)
        ));
    }
}
