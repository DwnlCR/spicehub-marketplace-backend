package br.com.dwnl.spicehub.identity.infrastructure.security.config;

import br.com.dwnl.spicehub.identity.infrastructure.security.jwt.JwtProperties;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties){
        byte[] decodedKey = Base64.getDecoder().decode(properties.secret());

        return new SecretKeySpec(decodedKey, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey){
        return NimbusJwtEncoder.withSecretKey(secretKey).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey, JwtProperties properties){
        NimbusJwtDecoder decoder =  NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        properties.issuer()
        ));

        return decoder;

    }
}
