package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.model.AuthenticatedUser;
import br.com.dwnl.spicehub.identity.application.port.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetCurrentUserUseCase {
    private final CurrentUserProvider currentUserProvider;

    public AuthenticatedUser execute(){
        return  currentUserProvider.getCurrentUser();
    }
}
