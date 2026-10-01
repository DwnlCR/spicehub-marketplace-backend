package br.com.dwnl.spicehub.identity.infrastructure.security.registration;

import br.com.dwnl.spicehub.config.PostgresTestContainerConfig;
import br.com.dwnl.spicehub.config.RedisTestContainerConfig;
import br.com.dwnl.spicehub.identity.application.port.RegistrationAttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import({
        PostgresTestContainerConfig.class,
        RedisTestContainerConfig.class
})
class RedisRegistrationAttemptServiceTest {

    private final RegistrationAttemptService registrationAttemptService;

    @Autowired
    RedisRegistrationAttemptServiceTest(
            RegistrationAttemptService registrationAttemptService
    ) {
        this.registrationAttemptService = registrationAttemptService;
    }

    @Test
    void shouldBlockAfterMaximumRegistrationAttempts() {

        String clientIp = "192.168.0.20";

        assertFalse(registrationAttemptService.isBlocked(clientIp));

        for (int i = 0; i < 5; i++) {
            registrationAttemptService.recordAttempt(clientIp);
        }

        assertTrue(registrationAttemptService.isBlocked(clientIp));
    }
}