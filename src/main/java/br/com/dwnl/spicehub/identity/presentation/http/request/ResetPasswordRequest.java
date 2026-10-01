package br.com.dwnl.spicehub.identity.presentation.http.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 320, message = "Email must have at most 320 characters")
        String email,

        @NotBlank(message = "Code is required")
        @Pattern(
                regexp = "\\d{6}",
                message = "Code must contain exactly 6 digits"
        )
        String code,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 72,
                message = "Password must have between 8 and 72 characters"
        )
        String newPassword
) {

}
