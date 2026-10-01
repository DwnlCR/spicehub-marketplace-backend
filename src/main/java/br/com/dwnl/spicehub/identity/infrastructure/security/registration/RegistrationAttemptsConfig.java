package br.com.dwnl.spicehub.identity.infrastructure.security.registration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RegistrationAttemptProperties.class)
public class RegistrationAttemptsConfig {
}
