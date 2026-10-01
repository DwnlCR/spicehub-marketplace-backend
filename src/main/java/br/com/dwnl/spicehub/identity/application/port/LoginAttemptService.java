package br.com.dwnl.spicehub.identity.application.port;

import br.com.dwnl.spicehub.identity.domain.model.Email;

public interface LoginAttemptService {


    boolean isBlocked(Email email,String clientIp);
    void recordFailure(Email email, String clientIp);
    void reset(Email email, String clientIp);
}
