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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyEmailUseCase {

    private final UserRepository userRepository;
    private final EmailVerificationCodeService emailVerificationCodeService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResult execute(String rawEmail, String code){
        Email email = new Email(rawEmail);

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidEmailVerificationCodeException::new);

        if (user.isEmailVerified()){
            throw new InvalidEmailVerificationCodeException();
        }

        boolean valid = emailVerificationCodeService.validateAndConsume(email, code);

        if (!valid){
            throw new InvalidEmailVerificationCodeException();
        }

        user.verifyEmail();

        User verifiedUser = userRepository.save(user);

        AccessToken accessToken = accessTokenService.generate(verifiedUser);
        RefreshToken refreshToken = refreshTokenService.generate(verifiedUser);

        return new LoginResult(verifiedUser, accessToken, refreshToken);
    }
}
