package br.com.dwnl.spicehub.identity.infrastructure.security.emailverification;

import br.com.dwnl.spicehub.config.PostgresTestContainerConfig;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.config.RedisTestContainerConfig;
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
class RedisEmailVerificationCodeRepositoryTest {

    @Autowired
    private RedisEmailVerificationCodeRepository repository;

    @Test
    void shouldAcquireResendCooldownOnlyOnceWhileTtlIsActive() {
        Email email = new Email("cooldown-once@gmail.com");

        boolean firstAttempt = repository.acquireResendCooldown(
                email,
                Duration.ofSeconds(5)
        );

        boolean secondAttempt = repository.acquireResendCooldown(
                email,
                Duration.ofSeconds(5)
        );

        assertTrue(firstAttempt);
        assertFalse(secondAttempt);
    }

    @Test
    void shouldAllowResendAgainAfterCooldownExpires()
            throws InterruptedException {

        Email email = new Email("cooldown-expiration@gmail.com");

        boolean firstAttempt = repository.acquireResendCooldown(
                email,
                Duration.ofMillis(200)
        );

        assertTrue(firstAttempt);

        Thread.sleep(300);

        boolean secondAttempt = repository.acquireResendCooldown(
                email,
                Duration.ofMillis(200)
        );

        assertTrue(secondAttempt);
    }
}