package br.com.dwnl.spicehub.identity.presentation.http.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestPasswordResetRequest(

        @NotBlank
        @Email
        String email
) {
}
