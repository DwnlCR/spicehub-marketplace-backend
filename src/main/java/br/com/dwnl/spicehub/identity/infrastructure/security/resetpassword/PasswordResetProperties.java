package br.com.dwnl.spicehub.identity.infrastructure.security.resetpassword;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.password-reset")
public record PasswordResetProperties(
        Duration codeTtl,
        Duration requestCooldown,
        int maxAttempts
) {
}
