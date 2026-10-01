package br.com.dwnl.spicehub.identity.presentation.http.exception;

import br.com.dwnl.spicehub.identity.application.exception.*;
import br.com.dwnl.spicehub.identity.domain.exception.DefaultRoleRemovalException;
import br.com.dwnl.spicehub.identity.domain.exception.InvalidEmailException;
import br.com.dwnl.spicehub.identity.domain.exception.InvalidUserNameException;
import br.com.dwnl.spicehub.identity.infrastructure.persistence.exception.RoleNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException exception){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Email already registered");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ProblemDetail handleInvalidEmail(InvalidEmailException exception){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Invalid email");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(InvalidUserNameException.class)
    public ProblemDetail handleInvalidUserName(InvalidUserNameException exception){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Invalid user name");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(DefaultRoleRemovalException.class)
    public ProblemDetail handleDefaultRoleRemoval(DefaultRoleRemovalException exception){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Role operation not allowed");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ProblemDetail handleRoleNotFound(RoleNotFoundException exception){

        log.error("Role persistence inconsistency", exception);

        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        problem.setTitle("Internal persistence error");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException exception){

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "One or more request fields are invalid"
        );

        problem.setTitle("Validation failed");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errors", errors);

        return problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException exception){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage()
        );

        problem.setTitle("Authentication failed");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(UserDisabledException.class)
    public ProblemDetail handleUserDisabled(UserDisabledException exception){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage()
        );

        problem.setTitle("Authentication failed");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRefreshToken(
            InvalidRefreshTokenException exception,
            HttpServletRequest request
    ) {

        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.UNAUTHORIZED
        );

        problem.setTitle("Invalid refresh token");
        problem.setDetail("Invalid or expired refresh token");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(problem);
    }

    @ExceptionHandler(AuthenticatedUserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticatedUserNotFound(
            AuthenticatedUserNotFoundException exception,
            HttpServletRequest request
            ){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user is no longer available"
        );

        problem.setTitle("Unauthorized");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(InvalidEmailVerificationCodeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidEmailVerificationCodeException(
            InvalidEmailVerificationCodeException exception
    ){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Invalid email verification code");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ProblemDetail> handleEmailNotVerified(
            EmailNotVerifiedException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
        );

        problem.setTitle("Email not verified");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(problem);
    }

    @ExceptionHandler(InvalidPasswordResetCodeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidPasswordResetCode(
            InvalidPasswordResetCodeException exception
    ){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Invalid password reset code");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ProblemDetail> handleEmailSending(
            EmailSendingException exception
    ){

        log.error("Failed to send email", exception);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to send email"
        );

        problem.setTitle("Email delivery failed");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler(TooManyRegistrationAttemptsException.class)
    public ResponseEntity<ProblemDetail> handleTooManyRegistrationAttempts(
            TooManyRegistrationAttemptsException exception
    ){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                exception.getMessage()
        );

        problem.setTitle("Too many registration attempts");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(problem);
    }
}
