package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.exception.InvalidPasswordResetCodeException;
import br.com.dwnl.spicehub.identity.application.port.PasswordEncoder;
import br.com.dwnl.spicehub.identity.application.port.PasswordResetCodeService;
import br.com.dwnl.spicehub.identity.application.port.RefreshTokenService;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResetPasswordUseCaseTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetCodeService passwordResetCodeService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    private ResetPasswordUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ResetPasswordUseCase(
                userRepository,
                passwordResetCodeService,
                passwordEncoder,
                refreshTokenService
        );
    }

    @Test
    void shouldResetPasswordAndRevokeSessionsWhenCodeIsValid() {
        Email email = new Email("reset@gmail.com");

        User user = User.create(
                "Daniel",
                email,
                "old-encoded-password"
        );

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordResetCodeService.validateAndConsume(
                email,
                "525127"
        )).thenReturn(true);

        when(passwordEncoder.encode("new-password"))
                .thenReturn("new-encoded-password");

        useCase.execute(
                email.value(),
                "525127",
                "new-password"
        );

        verify(passwordResetCodeService)
                .validateAndConsume(email, "525127");

        verify(passwordEncoder)
                .encode("new-password");

        verify(userRepository)
                .save(user);

        verify(refreshTokenService)
                .revokeAllByUserId(user.getId());
    }

    @Test
    void shouldThrowWhenPasswordResetCodeIsInvalid() {
        Email email = new Email("reset@gmail.com");

        User user = User.create(
                "Daniel",
                email,
                "old-encoded-password"
        );

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordResetCodeService.validateAndConsume(
                email,
                "525127"
        )).thenReturn(false);

        assertThrows(
                InvalidPasswordResetCodeException.class,
                () -> useCase.execute(
                        email.value(),
                        "525127",
                        "new-password"
                )
        );

        verify(passwordResetCodeService)
                .validateAndConsume(email, "525127");

        verifyNoInteractions(passwordEncoder);

        verify(userRepository, never())
                .save(any());

        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        Email email = new Email("unknown@gmail.com");

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidPasswordResetCodeException.class,
                () -> useCase.execute(
                        email.value(),
                        "525127",
                        "new-password"
                )
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(passwordResetCodeService);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(refreshTokenService);

        verify(userRepository, never())
                .save(any());
    }
}
