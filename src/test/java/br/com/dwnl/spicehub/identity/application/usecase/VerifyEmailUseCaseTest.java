package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.exception.InvalidEmailVerificationCodeException;
import br.com.dwnl.spicehub.identity.application.model.AccessToken;
import br.com.dwnl.spicehub.identity.application.model.LoginResult;
import br.com.dwnl.spicehub.identity.application.model.RefreshToken;
import br.com.dwnl.spicehub.identity.application.port.AccessTokenService;
import br.com.dwnl.spicehub.identity.application.port.EmailVerificationCodeService;
import br.com.dwnl.spicehub.identity.application.port.RefreshTokenService;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyEmailUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationCodeService emailVerificationCodeService;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private User user;

    @Mock
    private AccessToken accessToken;

    @Mock
    private RefreshToken refreshToken;

    @InjectMocks
    private VerifyEmailUseCase verifyEmailUseCase;

    private final String email = "user@gmail.com";
    private final String code = "123456";

    @Test
    void shouldVerifyEmailAndGenerateTokens() {
        when(userRepository.findByEmail(new Email(email)))
                .thenReturn(Optional.of(user));

        when(user.isEmailVerified()).thenReturn(false);

        when(emailVerificationCodeService.validateAndConsume(
                new Email(email), code
        )).thenReturn(true);

        when(userRepository.save(user)).thenReturn(user);
        when(accessTokenService.generate(user)).thenReturn(accessToken);
        when(refreshTokenService.generate(user)).thenReturn(refreshToken);

        LoginResult result = verifyEmailUseCase.execute(email, code);

        assertNotNull(result);
        assertSame(user, result.user());
        assertSame(accessToken, result.accessToken());
        assertSame(refreshToken, result.refreshToken());

        verify(user).verifyEmail();
        verify(userRepository).save(user);
        verify(accessTokenService).generate(user);
        verify(refreshTokenService).generate(user);
    }

    @Test
    void shouldRejectInvalidVerificationCode() {
        when(userRepository.findByEmail(new Email(email)))
                .thenReturn(Optional.of(user));

        when(user.isEmailVerified()).thenReturn(false);

        when(emailVerificationCodeService.validateAndConsume(
                new Email(email), code
        )).thenReturn(false);

        assertThrows(
                InvalidEmailVerificationCodeException.class,
                () -> verifyEmailUseCase.execute(email, code)
        );

        verify(user, never()).verifyEmail();
        verify(userRepository, never()).save(any());
        verifyNoInteractions(accessTokenService, refreshTokenService);
    }

    @Test
    void shouldRejectAlreadyVerifiedEmail() {
        when(userRepository.findByEmail(new Email(email)))
                .thenReturn(Optional.of(user));

        when(user.isEmailVerified()).thenReturn(true);

        assertThrows(
                InvalidEmailVerificationCodeException.class,
                () -> verifyEmailUseCase.execute(email, code)
        );

        verifyNoInteractions(
                emailVerificationCodeService,
                accessTokenService,
                refreshTokenService
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectUnknownEmail() {
        when(userRepository.findByEmail(new Email(email)))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidEmailVerificationCodeException.class,
                () -> verifyEmailUseCase.execute(email, code)
        );

        verifyNoInteractions(
                emailVerificationCodeService,
                accessTokenService,
                refreshTokenService
        );

        verify(userRepository, never()).save(any());
    }
}