package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.port.EmailVerificationCodeService;
import br.com.dwnl.spicehub.identity.application.port.EmailVerificationEmailSender;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResendEmailVerificationCodeUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationCodeService verificationCodeService;

    @Mock
    private EmailVerificationEmailSender verificationEmailSender;

    private ResendEmailVerificationCodeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ResendEmailVerificationCodeUseCase(
                userRepository,
                verificationCodeService,
                verificationEmailSender
        );
    }

    @Test
    void shouldSendNewVerificationCodeWhenCooldownIsAcquired() {
        Email email = new Email("resend@gmail.com");
        User user = createUnverifiedUser(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(verificationCodeService.acquireResendCooldown(email))
                .thenReturn(true);

        when(verificationCodeService.create(email))
                .thenReturn("525127");

        useCase.execute(email.value());

        verify(verificationCodeService)
                .acquireResendCooldown(email);

        verify(verificationCodeService)
                .create(email);

        verify(verificationEmailSender)
                .send(email, "525127");
    }

    @Test
    void shouldNotSendNewVerificationCodeWhenCooldownIsActive() {
        Email email = new Email("cooldown@gmail.com");
        User user = createUnverifiedUser(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(verificationCodeService.acquireResendCooldown(email))
                .thenReturn(false);

        useCase.execute(email.value());

        verify(verificationCodeService)
                .acquireResendCooldown(email);

        verify(verificationCodeService, never())
                .create(any());

        verifyNoInteractions(verificationEmailSender);
    }

    @Test
    void shouldDoNothingWhenUserDoesNotExist() {
        Email email = new Email("unknown@gmail.com");

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        useCase.execute(email.value());

        verifyNoInteractions(verificationCodeService);
        verifyNoInteractions(verificationEmailSender);
    }

    @Test
    void shouldDoNothingWhenEmailIsAlreadyVerified() {
        Email email = new Email("verified@gmail.com");
        User user = createUnverifiedUser(email);

        user.verifyEmail();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        useCase.execute(email.value());

        verifyNoInteractions(verificationCodeService);
        verifyNoInteractions(verificationEmailSender);
    }

    private User createUnverifiedUser(Email email) {
        return User.create(
                "Daniel",
                email,
                "encoded-password"
        );
    }
}