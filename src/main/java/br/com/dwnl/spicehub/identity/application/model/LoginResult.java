package br.com.dwnl.spicehub.identity.application.model;

import br.com.dwnl.spicehub.identity.domain.model.User;

public record LoginResult(User user, AccessToken accessToken) {
}
