package br.com.dwnl.spicehub.identity.infrastructure.security.login;

import br.com.dwnl.spicehub.config.PostgresTestContainerConfig;
import br.com.dwnl.spicehub.config.RedisTestContainerConfig;
import br.com.dwnl.spicehub.identity.application.port.LoginAttemptService;
import br.com.dwnl.spicehub.identity.domain.model.Email;
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
class RedisLoginAttemptServiceTest {

    private final LoginAttemptService loginAttemptService;

    @Autowired
    RedisLoginAttemptServiceTest(
            LoginAttemptService loginAttemptService
    ) {
        this.loginAttemptService = loginAttemptService;
    }

    @Test
    void shouldBlockAfterMaximumFailedAttempts() {

        Email email = new Email("blocked.login@gmail.com");
        String clientIp = "192.168.0.10";

        assertFalse(loginAttemptService.isBlocked(email, clientIp));

        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailure(email, clientIp);
        }

        assertTrue(loginAttemptService.isBlocked(email, clientIp));
    }

    @Test
    void shouldResetFailedAttempts() {

        Email email = new Email("reset.login@gmail.com");
        String clientIp = "192.168.0.11";

        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailure(email, clientIp);
        }

        assertTrue(loginAttemptService.isBlocked(email, clientIp));

        loginAttemptService.reset(email, clientIp);

        assertFalse(loginAttemptService.isBlocked(email, clientIp));
    }
}