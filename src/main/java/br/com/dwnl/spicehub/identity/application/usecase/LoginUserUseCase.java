package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.exception.InvalidCredentialsException;
import br.com.dwnl.spicehub.identity.application.exception.UserDisabledException;
import br.com.dwnl.spicehub.identity.application.model.AccessToken;
import br.com.dwnl.spicehub.identity.application.model.LoginResult;
import br.com.dwnl.spicehub.identity.application.port.AccessTokenService;
import br.com.dwnl.spicehub.identity.application.port.PasswordEncoder;
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

    @Transactional(readOnly = true)
    public LoginResult execute(String email, String password){
        Email userEmail = new Email(email);

        User user = userRepository.findByEmail(userEmail).orElseThrow(InvalidCredentialsException::new);

        if (!user.isEnabled()){
            throw new UserDisabledException();
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())){
            throw new InvalidCredentialsException();
        }

        AccessToken accessToken = accessTokenService.generate(user);

        return new LoginResult(user, accessToken);
    }
}
