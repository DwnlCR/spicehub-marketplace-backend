package br.com.dwnl.spicehub.identity.infrastructure.security.login;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.login-attempts")
public record LoginAttemptProperties(
        int maxAttempts,
        Duration window
) {
}
