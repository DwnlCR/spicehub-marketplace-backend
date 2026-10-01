package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.exception.EmailNotVerifiedException;
import br.com.dwnl.spicehub.identity.application.exception.InvalidCredentialsException;
import br.com.dwnl.spicehub.identity.application.exception.UserDisabledException;
import br.com.dwnl.spicehub.identity.application.model.AccessToken;
import br.com.dwnl.spicehub.identity.application.model.LoginResult;
import br.com.dwnl.spicehub.identity.application.model.RefreshToken;
import br.com.dwnl.spicehub.identity.application.port.AccessTokenService;
import br.com.dwnl.spicehub.identity.application.port.LoginAttemptService;
import br.com.dwnl.spicehub.identity.application.port.PasswordEncoder;
import br.com.dwnl.spicehub.identity.application.port.RefreshTokenService;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    @Transactional(readOnly = true)
    public LoginResult execute(String email, String password, String clientIp){
        Email userEmail = new Email(email);

        if (loginAttemptService.isBlocked(userEmail, clientIp)){
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(userEmail).
                orElse(null);

        if (user == null){
            loginAttemptService.recordFailure(userEmail, clientIp);
            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled()){
            throw new UserDisabledException();
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())){
            loginAttemptService.recordFailure(userEmail, clientIp);
            throw new InvalidCredentialsException();
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        loginAttemptService.reset(userEmail, clientIp);

        AccessToken accessToken = accessTokenService.generate(user);
        RefreshToken refreshToken = refreshTokenService.generate(user);

        return new LoginResult(user, accessToken, refreshToken);
    }
}
