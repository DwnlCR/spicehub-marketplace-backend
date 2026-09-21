package br.com.dwnl.spicehub.identity.application.port;

import br.com.dwnl.spicehub.identity.application.model.AuthenticatedUser;

public interface CurrentUserProvider {

    AuthenticatedUser getCurrentUser();
}
