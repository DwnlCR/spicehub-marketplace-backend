package br.com.dwnl.spicehub.identity.presentation.http.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 320, message = "Email must have at most 320 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                max = 72,
                message = "Password must have at most 72 characters"
        )
        String password
) {
}
