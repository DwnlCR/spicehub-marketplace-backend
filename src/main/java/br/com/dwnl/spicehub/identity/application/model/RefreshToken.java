package br.com.dwnl.spicehub.identity.application.model;

import java.time.Instant;

public record RefreshToken(String value, Instant expiredAt) {
}
