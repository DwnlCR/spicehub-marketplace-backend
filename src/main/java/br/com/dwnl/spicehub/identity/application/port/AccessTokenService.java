package br.com.dwnl.spicehub.identity.application.port;

import br.com.dwnl.spicehub.identity.application.model.AccessToken;
import br.com.dwnl.spicehub.identity.domain.model.User;

public interface AccessTokenService {
    AccessToken generate(User user);
}
