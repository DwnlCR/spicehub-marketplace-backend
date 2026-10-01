package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.exception.EmailSendingException;
import br.com.dwnl.spicehub.identity.application.exception.TooManyRegistrationAttemptsException;
import br.com.dwnl.spicehub.identity.application.port.EmailVerificationCodeService;
import br.com.dwnl.spicehub.identity.application.port.EmailVerificationEmailSender;
import br.com.dwnl.spicehub.identity.application.port.RegistrationAttemptService;
import br.com.dwnl.spicehub.identity.application.result.RegisterUserResult;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final CreateUserUseCase createUserUseCase;
    private final EmailVerificationCodeService emailVerificationCodeService;
    private final EmailVerificationEmailSender emailVerificationEmailSender;
    private final RegistrationAttemptService registrationAttemptService;

    public RegisterUserResult execute(String name, String email, String password, String clientId) {
        if (registrationAttemptService.isBlocked(clientId)){
            throw new TooManyRegistrationAttemptsException();
        }

        registrationAttemptService.recordAttempt(clientId);

        Email userEmail = new Email(email);

        User user = createUserUseCase.execute(name, userEmail, password);

        String verificationCode = emailVerificationCodeService.create(userEmail);

        boolean verificationEmailSent;

        try {
            emailVerificationEmailSender.send(
                    userEmail,
                    verificationCode
            );

            verificationEmailSent = true;
        } catch (EmailSendingException exception) {
            verificationEmailSent = false;
        }

        return new RegisterUserResult(user, verificationEmailSent);
    }
}