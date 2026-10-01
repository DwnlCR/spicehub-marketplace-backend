package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.exception.EmailSendingException;
import br.com.dwnl.spicehub.identity.application.port.PasswordResetCodeService;
import br.com.dwnl.spicehub.identity.application.port.PasswordResetEmailSender;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class RequestPasswordResetUseCase {

    private final UserRepository userRepository;
    private final PasswordResetCodeService passwordResetCodeService;
    private final PasswordResetEmailSender passwordResetEmailSender;

    public void execute(String rawEmail){
        Email email = new Email(rawEmail);

        if (userRepository.findByEmail(email).isEmpty()){
            return;
        }

        if (!passwordResetCodeService.acquireRequestCooldown(email)){
            return;
        }

        String code = passwordResetCodeService.create(email);

        try {
            passwordResetEmailSender.send(email, code);
        } catch (EmailSendingException exception){
            passwordResetCodeService.invalidate(email);
            throw exception;
        }
    }
}
