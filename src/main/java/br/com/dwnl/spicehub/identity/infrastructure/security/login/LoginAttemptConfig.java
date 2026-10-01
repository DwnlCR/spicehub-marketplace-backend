package br.com.dwnl.spicehub.identity.infrastructure.security.login;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LoginAttemptProperties.class)
public class LoginAttemptConfig {
}
