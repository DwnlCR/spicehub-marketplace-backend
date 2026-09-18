package br.com.dwnl.spicehub.identity.infrastructure.security.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.refresh-cookie")
public record RefreshCookieProperties(boolean secure, String sameSite) {
}
