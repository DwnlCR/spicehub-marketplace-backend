package br.com.dwnl.spicehub.identity.infrastructure.security.emailverification;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.email-verification")
public record EmailVerificationProperties(Duration codeTtl, Duration resendCooldown) {
}
