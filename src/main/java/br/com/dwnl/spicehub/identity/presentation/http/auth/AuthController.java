package br.com.dwnl.spicehub.identity.presentation.http.auth;

import br.com.dwnl.spicehub.identity.application.exception.InvalidRefreshTokenException;
import br.com.dwnl.spicehub.identity.application.model.LoginResult;
import br.com.dwnl.spicehub.identity.application.port.RefreshTokenService;
import br.com.dwnl.spicehub.identity.application.usecase.*;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.infrastructure.security.cookie.RefreshTokenCookieService;
import br.com.dwnl.spicehub.identity.presentation.http.request.ResendEmailVerificationRequest;
import br.com.dwnl.spicehub.identity.presentation.http.request.VerifyEmailRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RefreshAccessTokenUseCase refreshAccessTokenUseCase;

    private final RefreshTokenCookieService refreshTokenCookieService;
    private final RefreshTokenService refreshTokenService;

    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendEmailVerificationCodeUseCase resendEmailVerificationCodeUseCase;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterUserResponse register(@Valid @RequestBody RegisterUserRequest request){

        User user = registerUserUseCase.execute(request.name(), request.email(), request.password());

        return RegisterUserResponse.from(user);
    }


    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {

        LoginResult result = loginUserUseCase.execute(
                request.email(),
                request.password()
        );

        ResponseCookie refreshCookie =
                refreshTokenCookieService.create(result.refreshToken());

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString()
        );

        return LoginResponse.from(result);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(HttpServletRequest request, HttpServletResponse response){
        String refreshTokenValue = extractRefreshToken(request);

        LoginResult result = refreshAccessTokenUseCase.execute(refreshTokenValue);

        ResponseCookie refreshCookie = refreshTokenCookieService.create(result.refreshToken());

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString()
        );

        return LoginResponse.from(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response){
        String refreshTokenValue = extractRefreshTokenOrNull(request);

        refreshTokenService.revoke(refreshTokenValue);

        ResponseCookie clearCookie = refreshTokenCookieService.clear();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                clearCookie.toString()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/csrf")
    public void csrf(CsrfToken csrfToken){

    }


    private String extractRefreshToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();

        if (cookies == null){
            throw new InvalidRefreshTokenException();
        }

        for (Cookie cookie : cookies){
            if ("refresh_token".equals(cookie.getName())){
                return cookie.getValue();
            }
        }

        throw new InvalidRefreshTokenException();
    }

    private String extractRefreshTokenOrNull(
            HttpServletRequest request
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("refresh_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@Valid @RequestBody VerifyEmailRequest request){

        verifyEmailUseCase.execute(request.email(), request.code());
    }

    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendVerification(@Valid @RequestBody ResendEmailVerificationRequest request){

        resendEmailVerificationCodeUseCase.execute(request.email());
    }
}
