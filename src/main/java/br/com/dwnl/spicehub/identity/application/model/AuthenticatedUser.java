package br.com.dwnl.spicehub.identity.application.model;

import br.com.dwnl.spicehub.identity.domain.model.RoleName;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(UUID id, String email, Set<RoleName> roles) {
}
