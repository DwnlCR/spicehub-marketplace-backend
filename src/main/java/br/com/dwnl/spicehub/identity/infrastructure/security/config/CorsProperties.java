package br.com.dwnl.spicehub.identity.infrastructure.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "security.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
