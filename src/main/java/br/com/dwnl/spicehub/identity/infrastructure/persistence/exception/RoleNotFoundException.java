package br.com.dwnl.spicehub.identity.infrastructure.persistence.exception;

import br.com.dwnl.spicehub.identity.domain.model.RoleName;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(RoleName roleName) {
        super("Role not found in database: " + roleName);
    }
}
