package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.exception.InvalidEmailVerificationCodeException;
import br.com.dwnl.spicehub.identity.application.port.EmailVerificationCodeService;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyEmailUseCase {

    private final UserRepository userRepository;
    private final EmailVerificationCodeService emailVerificationCodeService;

    @Transactional
    public void execute(String rawEmail, String code){
        Email email = new Email(rawEmail);

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidEmailVerificationCodeException::new);

        if (user.isEmailVerified()){
            return;
        }

        boolean valid = emailVerificationCodeService.validateAndConsume(email, code);

        if (!valid){
            throw new InvalidEmailVerificationCodeException();
        }

        user.verifyEmail();

        userRepository.save(user);
    }
}
