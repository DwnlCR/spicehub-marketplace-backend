package br.com.dwnl.spicehub.identity.presentation.http.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(
                regexp = "\\d{6}",
                message = "Verification code must contain exactly 6 digits"
        )
        String code
) {
}
