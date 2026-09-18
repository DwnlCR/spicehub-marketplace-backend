package br.com.dwnl.spicehub.identity.application.model;

import java.time.Instant;
import java.util.UUID;

public record RefreshSession(UUID id, UUID userId, String tokenHash, Instant createdAt, Instant expiresAt) {
}
