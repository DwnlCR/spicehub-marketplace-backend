package br.com.dwnl.spicehub.identity.infrastructure.security.authentication;

import br.com.dwnl.spicehub.identity.application.exception.UnauthenticatedUserException;
import br.com.dwnl.spicehub.identity.application.model.AuthenticatedUser;
import br.com.dwnl.spicehub.identity.application.port.CurrentUserProvider;
import br.com.dwnl.spicehub.identity.domain.model.RoleName;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpringSecurityCurrentUserProvider implements CurrentUserProvider {
    @Override
    public AuthenticatedUser getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)){
            throw new UnauthenticatedUserException();
        }

        UUID userId = UUID.fromString(jwt.getSubject());

        Set<RoleName> roles = jwt.getClaimAsStringList("roles")
                .stream()
                .map(RoleName::valueOf)
                .collect(Collectors.toSet());

        return new AuthenticatedUser(
                userId,
                jwt.getClaimAsString("email"),
                roles
        );
    }
}
