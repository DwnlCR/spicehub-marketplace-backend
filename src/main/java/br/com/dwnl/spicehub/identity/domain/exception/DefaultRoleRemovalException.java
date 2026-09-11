package br.com.dwnl.spicehub.identity.domain.exception;

public class DefaultRoleRemovalException extends IdentityDomainException {
    public DefaultRoleRemovalException() {
        super("Default USER role cannot be removed");
    }
}
