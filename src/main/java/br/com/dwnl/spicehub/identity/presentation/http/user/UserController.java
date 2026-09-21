package br.com.dwnl.spicehub.identity.presentation.http.user;

import br.com.dwnl.spicehub.identity.application.model.AuthenticatedUser;
import br.com.dwnl.spicehub.identity.application.usecase.GetCurrentUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @GetMapping("/me")
    public CurrentUserResponse me() {

        AuthenticatedUser user = getCurrentUserUseCase.execute();

        return new CurrentUserResponse(
                user.id().toString(),
                user.email(),
                user.roles().stream()
                        .map(Enum::name)
                        .toList()
        );
    }

    public record CurrentUserResponse(
            String id,
            String email,
            List<String> roles
    ) {
    }
}