package br.com.dwnl.spicehub.identity.infrastructure.email;

import br.com.dwnl.spicehub.identity.application.exception.EmailSendingException;
import br.com.dwnl.spicehub.identity.application.port.PasswordResetEmailSender;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResendPasswordResetEmailSender implements PasswordResetEmailSender {

    private final Resend resend;
    private final EmailSenderProperties properties;

    @Override
    public void send(Email email, String code) {
        CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                .from(properties.from())
                .to(email.value())
                .subject("Reset your SpiceHub password")
                .html(
                        """
                        <h2>Reset your password</h2>
                        <p>Your SpiceHub password reset code is:</p>
                        <h1>%s</h1>
                        <p>This code expires in 10 minutes.</p>
                        <p>If you did not request a password reset, ignore this email.</p>
                        """.formatted(code)
                )
                .build();

        try {
            resend.emails().send(emailOptions);
        } catch (ResendException exception) {
            throw new EmailSendingException("Failed to send password reset code" ,exception);
        }
    }
}
