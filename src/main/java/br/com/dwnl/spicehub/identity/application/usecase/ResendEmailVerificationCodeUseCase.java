package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.port.EmailVerificationCodeService;
import br.com.dwnl.spicehub.identity.application.port.EmailVerificationEmailSender;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResendEmailVerificationCodeUseCase {

    private final UserRepository userRepository;
    private final EmailVerificationCodeService verificationCodeService;
    private final EmailVerificationEmailSender verificationEmailSender;

    public void execute(String rawEmail) {
        Email email = new Email(rawEmail);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || user.isEmailVerified()) {
            return;
        }

        if (!verificationCodeService.acquireResendCooldown(email)) {
            return;
        }

        String code = verificationCodeService.create(email);

        verificationEmailSender.send(email, code);
    }
}