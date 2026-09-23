package br.com.dwnl.spicehub.identity.infrastructure.email;

import com.resend.Resend;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmailSenderProperties.class)
public class EmailSenderConfig {

    @Bean
    public Resend resend(EmailSenderProperties properties){
        return new Resend(properties.apiKey());
    }
}
