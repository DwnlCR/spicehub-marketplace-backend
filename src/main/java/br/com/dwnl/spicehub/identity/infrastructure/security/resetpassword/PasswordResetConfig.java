package br.com.dwnl.spicehub.identity.infrastructure.security.resetpassword;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PasswordResetProperties.class)
public class PasswordResetConfig{
}
