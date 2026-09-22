package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.exception.AuthenticatedUserNotFoundException;
import br.com.dwnl.spicehub.identity.application.exception.DisabledUserException;
import br.com.dwnl.spicehub.identity.application.model.AuthenticatedUser;
import br.com.dwnl.spicehub.identity.application.port.CurrentUserProvider;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCurrentUserProfileUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;

    public User execute() {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        User user = userRepository.findById(authenticatedUser.id())
                .orElseThrow(() ->
                        new AuthenticatedUserNotFoundException(authenticatedUser.id())
                );

        if (!user.isEnabled()) {
            throw new DisabledUserException();
        }

        return user;
    }
}