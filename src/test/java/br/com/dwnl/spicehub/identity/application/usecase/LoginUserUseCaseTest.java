package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.exception.InvalidCredentialsException;
import br.com.dwnl.spicehub.identity.application.port.AccessTokenService;
import br.com.dwnl.spicehub.identity.application.port.LoginAttemptService;
import br.com.dwnl.spicehub.identity.application.port.PasswordEncoder;
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
class LoginUserUseCaseTest {

    private static final String EMAIL = "daniel@gmail.com";
    private static final String PASSWORD = "password123";
    private static final String CLIENT_IP = "127.0.0.1";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private LoginAttemptService loginAttemptService;

    private LoginUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LoginUserUseCase(
                userRepository,
                passwordEncoder,
                accessTokenService,
                refreshTokenService,
                loginAttemptService
        );
    }

    @Test
    void shouldRecordFailureWhenUserDoesNotExist() {
        Email email = new Email(EMAIL);

        when(loginAttemptService.isBlocked(email, CLIENT_IP))
                .thenReturn(false);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> useCase.execute(EMAIL, PASSWORD, CLIENT_IP)
        );

        verify(loginAttemptService)
                .recordFailure(email, CLIENT_IP);

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(accessTokenService);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void shouldRecordFailureWhenPasswordIsInvalid() {
        Email email = new Email(EMAIL);
        User user = createVerifiedUser(email);

        when(loginAttemptService.isBlocked(email, CLIENT_IP))
                .thenReturn(false);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(PASSWORD, user.getPasswordHash()))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> useCase.execute(EMAIL, PASSWORD, CLIENT_IP)
        );

        verify(loginAttemptService)
                .recordFailure(email, CLIENT_IP);

        verifyNoInteractions(accessTokenService);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void shouldRejectLoginImmediatelyWhenBlocked() {
        Email email = new Email(EMAIL);

        when(loginAttemptService.isBlocked(email, CLIENT_IP))
                .thenReturn(true);

        assertThrows(
                InvalidCredentialsException.class,
                () -> useCase.execute(EMAIL, PASSWORD, CLIENT_IP)
        );

        verifyNoInteractions(userRepository);
        verify(loginAttemptService, never())
                .recordFailure(any(), any());

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(accessTokenService);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void shouldResetFailuresAfterSuccessfulLogin() {
        Email email = new Email(EMAIL);
        User user = createVerifiedUser(email);

        when(loginAttemptService.isBlocked(email, CLIENT_IP))
                .thenReturn(false);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(PASSWORD, user.getPasswordHash()))
                .thenReturn(true);

        useCase.execute(EMAIL, PASSWORD, CLIENT_IP);

        verify(loginAttemptService)
                .reset(email, CLIENT_IP);

        verify(accessTokenService).generate(user);
        verify(refreshTokenService).generate(user);

        verify(loginAttemptService, never())
                .recordFailure(any(), any());
    }

    private User createVerifiedUser(Email email) {
        User user = User.create(
                "Daniel",
                email,
                "encoded-password"
        );

        user.verifyEmail();

        return user;
    }
}