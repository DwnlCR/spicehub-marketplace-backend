package br.com.dwnl.spicehub.identity.domain.model;

import br.com.dwnl.spicehub.identity.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailTest {

    @Test
    void shouldAcceptAllowedEmailProviders() {
        assertDoesNotThrow(() -> new Email("user@gmail.com"));
        assertDoesNotThrow(() -> new Email("user@hotmail.com"));
        assertDoesNotThrow(() -> new Email("user@outlook.com"));
        assertDoesNotThrow(() -> new Email("user@yahoo.com"));
    }

    @Test
    void shouldRejectUnsupportedEmailProvider() {
        assertThrows(
                InvalidEmailException.class,
                () -> new Email("user@spicehub.com")
        );
    }

    @Test
    void shouldRejectDomainsThatTryToImpersonateAllowedProviders() {
        assertThrows(
                InvalidEmailException.class,
                () -> new Email("user@fakegmail.com")
        );

        assertThrows(
                InvalidEmailException.class,
                () -> new Email("user@gmail.com.br")
        );

        assertThrows(
                InvalidEmailException.class,
                () -> new Email("user@gmail.com.fake.com")
        );
    }

    @Test
    void shouldTreatEmailProviderAsCaseInsensitive() {
        assertDoesNotThrow(
                () -> new Email("user@GMAIL.COM")
        );
    }
}