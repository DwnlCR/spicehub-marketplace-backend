package br.com.dwnl.spicehub.identity.infrastructure.email;

import br.com.dwnl.spicehub.identity.application.port.EmailVerificationEmailSender;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResendEmailVerificationEmailSender implements EmailVerificationEmailSender {

    private final Resend resend;
    private final EmailSenderProperties properties;

    @Override
    public void send(Email email, String code) {
        CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                .from(properties.from())
                .to(email.value())
                .subject("Verify your SpiceHub account")
                .html("""
                        <h2>Verify your email</h2>
                        <p>Your SpiceHub verification code is:</p>
                        <h1>%s</h1>
                        <p>This code expires in 10 minutes.</p>
                        <p>If you did not create a SpiceHub account, ignore this email.</p>
                        """.formatted(code))
                .build();

        try {
            resend.emails().send(emailOptions);
        } catch (ResendException exception) {
            throw new EmailSendingException("Failed to send email verification code" ,exception);
        }
    }
}
