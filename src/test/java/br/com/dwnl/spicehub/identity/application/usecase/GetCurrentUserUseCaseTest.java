package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.model.AuthenticatedUser;
import br.com.dwnl.spicehub.identity.application.port.CurrentUserProvider;
import br.com.dwnl.spicehub.identity.domain.model.RoleName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserUseCaseTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private GetCurrentUserUseCase getCurrentUserUseCase;

    @Test
    void shouldReturnAuthenticatedUser() {
        UUID userId = UUID.randomUUID();

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                userId,
                "user@gmail.com",
                Set.of(RoleName.USER)
        );

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        AuthenticatedUser result = getCurrentUserUseCase.execute();

        assertEquals(authenticatedUser, result);

        verify(currentUserProvider).getCurrentUser();
    }
}