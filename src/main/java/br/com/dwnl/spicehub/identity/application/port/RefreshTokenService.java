package br.com.dwnl.spicehub.identity.application.port;

import br.com.dwnl.spicehub.identity.application.model.RefreshToken;
import br.com.dwnl.spicehub.identity.domain.model.User;

import java.util.UUID;

public interface RefreshTokenService {

    RefreshToken generate(User user);
    UUID validateAndConsume(String refreshToken);
    void revoke(String refreshToken);
}
