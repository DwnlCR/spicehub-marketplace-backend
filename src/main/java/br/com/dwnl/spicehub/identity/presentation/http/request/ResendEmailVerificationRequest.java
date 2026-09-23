package br.com.dwnl.spicehub.identity.presentation.http.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendEmailVerificationRequest(

        @NotBlank
        @Email
        String email
) {
}
