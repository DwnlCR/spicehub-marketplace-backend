package br.com.dwnl.spicehub.identity.application.model;

import java.time.Instant;

public record AccessToken(String value, Instant expiredAt) {
}
