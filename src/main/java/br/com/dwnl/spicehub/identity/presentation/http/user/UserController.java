package br.com.dwnl.spicehub.identity.presentation.http.user;

import br.com.dwnl.spicehub.identity.application.model.AuthenticatedUser;
import br.com.dwnl.spicehub.identity.application.usecase.GetCurrentUserProfileUseCase;
import br.com.dwnl.spicehub.identity.application.usecase.GetCurrentUserUseCase;
import br.com.dwnl.spicehub.identity.domain.model.User;
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

    private final GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;

    @GetMapping("/me")
    public CurrentUserResponse me() {
        User user = getCurrentUserProfileUseCase.execute();

        return new CurrentUserResponse(
                user.getId().toString(),
                user.getName(),
                user.getEmail().value(),
                user.getRoles().stream()
                        .map(Enum::name)
                        .toList()
        );
    }

    public record CurrentUserResponse(
            String id,
            String name,
            String email,
            List<String> roles
    ) {
    }
}