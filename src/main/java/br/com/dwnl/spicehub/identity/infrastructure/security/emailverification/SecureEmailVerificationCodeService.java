package br.com.dwnl.spicehub.identity.infrastructure.security.emailverification;

import br.com.dwnl.spicehub.identity.application.port.EmailVerificationCodeRepository;
import br.com.dwnl.spicehub.identity.application.port.EmailVerificationCodeService;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.infrastructure.security.exception.CryptographicOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class SecureEmailVerificationCodeService implements EmailVerificationCodeService {

    private static final int CODE_BOUND = 1_000_000;

    private final EmailVerificationCodeRepository codeRepository;
    private final EmailVerificationProperties verificationProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String create(Email email) {
        String code = generateCode();
        String codeHash = hash(code);

        codeRepository.save(
                email,
                codeHash,
                verificationProperties.codeTtl()
        );

        return code;
    }

    @Override
    public boolean validateAndConsume(Email email, String code) {
        if (code == null || !code.matches("\\d{6}")) {
            return false;
        }

        String codeHash = hash(code);

        return codeRepository.consumeIfMatches(email, codeHash);
    }

    @Override
    public boolean acquireResendCooldown(Email email) {
        return codeRepository.acquireResendCooldown(
                email,
                verificationProperties.resendCooldown()
        );
    }

    private String generateCode() {
        int value = secureRandom.nextInt(CODE_BOUND);

        return "%06d".formatted(value);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new CryptographicOperationException(
                    "Failed to hash email verification code",
                    exception
            );
        }
    }
}