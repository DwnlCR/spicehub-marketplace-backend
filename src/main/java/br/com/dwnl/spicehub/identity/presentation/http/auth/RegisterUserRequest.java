package br.com.dwnl.spicehub.identity.presentation.http.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must have at most 120 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 320, message = "Email must have at most 320 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8 ,max = 72, message = "Password must have between 8 and 72 characters")
        String password

) {
}
