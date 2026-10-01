package br.com.dwnl.spicehub.identity.application.port;

public interface RegistrationAttemptService {

    boolean isBlocked(String clientIp);

    void recordAttempt(String clientIp);
}
