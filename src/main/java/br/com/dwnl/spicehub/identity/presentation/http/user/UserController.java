package br.com.dwnl.spicehub.identity.presentation.http.user;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/me")
    public CurrentUserResponse me(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return new CurrentUserResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsStringList("roles")
        );
    }

    public record CurrentUserResponse(
            String id,
            String email,
            List<String> roles
    ) {
    }
}