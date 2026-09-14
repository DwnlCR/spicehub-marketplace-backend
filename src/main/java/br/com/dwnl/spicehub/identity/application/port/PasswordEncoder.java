package br.com.dwnl.spicehub.identity.application.port;

public interface PasswordEncoder {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
