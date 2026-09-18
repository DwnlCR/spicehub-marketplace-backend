package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.exception.InvalidRefreshTokenException;
import br.com.dwnl.spicehub.identity.application.model.AccessToken;
import br.com.dwnl.spicehub.identity.application.model.LoginResult;
import br.com.dwnl.spicehub.identity.application.model.RefreshToken;
import br.com.dwnl.spicehub.identity.application.port.AccessTokenService;
import br.com.dwnl.spicehub.identity.application.port.RefreshTokenService;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshAccessTokenUseCase {

    private final UserRepository userRepository;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    public LoginResult execute(String refreshTokenValue){
        UUID userId = refreshTokenService.validateAndConsume(refreshTokenValue);

        User user = userRepository.findById(userId)
                .orElseThrow(InvalidRefreshTokenException::new);

        if(!user.isEnabled()){
            throw new InvalidRefreshTokenException();
        }

        AccessToken accessToken = accessTokenService.generate(user);
        RefreshToken refreshToken = refreshTokenService.generate(user);

        return new LoginResult(
                user,
                accessToken,
                refreshToken
        );
    }
}
