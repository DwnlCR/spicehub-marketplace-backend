package br.com.dwnl.spicehub.identity.presentation.http.auth;

import br.com.dwnl.spicehub.identity.application.model.LoginResult;
import br.com.dwnl.spicehub.identity.application.usecase.LoginUserUseCase;
import br.com.dwnl.spicehub.identity.application.usecase.RegisterUserUseCase;
import br.com.dwnl.spicehub.identity.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterUserResponse register(@Valid @RequestBody RegisterUserRequest request){

        User user = registerUserUseCase.execute(request.name(), request.email(), request.password());

        return RegisterUserResponse.from(user);
    }


    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request){

        LoginResult result = loginUserUseCase.execute(request.email(), request.password());

        return LoginResponse.from(result);
    }
}
