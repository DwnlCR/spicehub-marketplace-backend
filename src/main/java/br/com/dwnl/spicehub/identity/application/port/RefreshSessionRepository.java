package br.com.dwnl.spicehub.identity.application.port;

import br.com.dwnl.spicehub.identity.application.model.RefreshSession;

import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionRepository {

    void save(RefreshSession session);

    Optional<RefreshSession> findById(UUID sessionId);

    void deleteById(UUID sessionId);

    Optional<RefreshSession> consumeIfMatches(UUID sessionId, String tokenHash);
}
