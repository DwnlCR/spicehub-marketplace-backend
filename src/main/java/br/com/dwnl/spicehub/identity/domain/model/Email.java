package br.com.dwnl.spicehub.identity.domain.model;

import br.com.dwnl.spicehub.identity.domain.exception.InvalidEmailException;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class Email {

    private static final int MAX_LENGTH = 320;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Set<String> ALLOWED_DOMAINS = Set.of(
            "gmail.com",
            "hotmail.com",
            "outlook.com",
            "yahoo.com"
    );

    private final String value;

    public Email(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidEmailException("Email cannot be null or blank");
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        if (normalized.length() > MAX_LENGTH) {
            throw new InvalidEmailException("Email exceeds the maximum length");
        }

        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidEmailException("Invalid email format");
        }

        String domain = normalized.substring(normalized.lastIndexOf('@') + 1);

        if (!ALLOWED_DOMAINS.contains(domain)) {
            throw new InvalidEmailException("Email provider is not supported");
        }

        this.value = normalized;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Email email)) {
            return false;
        }

        return value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}