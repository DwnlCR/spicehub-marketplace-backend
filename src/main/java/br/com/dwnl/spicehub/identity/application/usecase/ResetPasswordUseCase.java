package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.exception.InvalidPasswordResetCodeException;
import br.com.dwnl.spicehub.identity.application.port.PasswordEncoder;
import br.com.dwnl.spicehub.identity.application.port.PasswordResetCodeService;
import br.com.dwnl.spicehub.identity.application.port.RefreshTokenService;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetCodeService passwordResetCodeService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void execute(String rawEmail, String code, String newPassword){
        Email email = new Email(rawEmail);

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidPasswordResetCodeException::new);

        boolean valid = passwordResetCodeService.validateAndConsume(email, code);

        if (!valid){
            throw new InvalidPasswordResetCodeException();
        }

        String passwordHash = passwordEncoder.encode(newPassword);

        user.changePassword(passwordHash);

        userRepository.save(user);

        refreshTokenService.revokeAllByUserId(user.getId());
    }
}
