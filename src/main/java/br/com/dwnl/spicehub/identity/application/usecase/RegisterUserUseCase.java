package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.exception.EmailAlreadyExistsException;
import br.com.dwnl.spicehub.identity.application.port.EmailVerificationCodeService;
import br.com.dwnl.spicehub.identity.application.port.EmailVerificationEmailSender;
import br.com.dwnl.spicehub.identity.application.port.PasswordEncoder;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final CreateUserUseCase createUserUseCase;
    private final EmailVerificationCodeService emailVerificationCodeService;
    private final EmailVerificationEmailSender emailVerificationEmailSender;

    public User execute(String name, String email, String password){
        Email userEmail = new Email(email);

        User user = createUserUseCase.execute(name, userEmail, password);

        String verificationCode = emailVerificationCodeService.create(userEmail);

        emailVerificationEmailSender.send(userEmail, verificationCode);

        return user;
    }
}
