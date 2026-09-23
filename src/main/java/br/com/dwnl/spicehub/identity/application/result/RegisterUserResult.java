package br.com.dwnl.spicehub.identity.application.result;

import br.com.dwnl.spicehub.identity.domain.model.User;

public record RegisterUserResult(
        User user,
        boolean verificationEmailSent
) {
}
