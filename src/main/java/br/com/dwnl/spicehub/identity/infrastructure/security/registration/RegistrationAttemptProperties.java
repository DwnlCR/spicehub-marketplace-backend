package br.com.dwnl.spicehub.identity.infrastructure.security.registration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.registration-attempts")
public record RegistrationAttemptProperties(
        int maxAttempts,
        Duration window
) {
}
