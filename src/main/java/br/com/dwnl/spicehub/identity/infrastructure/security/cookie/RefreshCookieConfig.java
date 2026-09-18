package br.com.dwnl.spicehub.identity.infrastructure.security.cookie;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RefreshCookieProperties.class)
public class RefreshCookieConfig {

}
