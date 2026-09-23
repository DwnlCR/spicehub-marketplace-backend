package br.com.dwnl.spicehub.identity.infrastructure.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "email.sender")
public record EmailSenderProperties(
        String apiKey,
        String from
) {
}
