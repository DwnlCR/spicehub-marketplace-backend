package br.com.dwnl.spicehub.identity.infrastructure.security.refresh;

import br.com.dwnl.spicehub.identity.application.model.RefreshSession;
import br.com.dwnl.spicehub.identity.application.port.RefreshSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisRefreshSessionRepository implements RefreshSessionRepository {

    private static final String KEY_PREFIX = "auth:refresh:";

    private static final String USER_SESSIONS_KEY_PREFIX = "auth:refresh:user:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(RefreshSession session) {
        String key = buildKey(session.id());

        Duration ttl = Duration.between(Instant.now(), session.expiresAt());

        if (ttl.isNegative() || ttl.isZero()){
            return;
        }

        String value = objectMapper.writeValueAsString(session);

        redisTemplate.opsForValue().set(key, value, ttl);

        String userSessionKey = buildUserSessionsKey(session.userId());

        redisTemplate.opsForSet().add(userSessionKey, session.id().toString());

        redisTemplate.expire(userSessionKey, ttl);
    }

    @Override
    public Optional<RefreshSession> findById(UUID sessionId) {
        String value = redisTemplate.opsForValue().get(buildKey(sessionId));

        if (value == null){
            return Optional.empty();
        }

        RefreshSession session = objectMapper.readValue(value, RefreshSession.class);

        return Optional.of(session);
    }

    @Override
    public void deleteById(UUID sessionId) {

        String key = buildKey(sessionId);

        String value = redisTemplate.opsForValue().get(key);

        if (value == null){
            return;
        }

        RefreshSession session = objectMapper.readValue(value, RefreshSession.class);

        redisTemplate.delete(key);

        redisTemplate.opsForSet().remove(buildUserSessionsKey(session.userId()), sessionId.toString());
    }

    private static final DefaultRedisScript<String> CONSUME_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local value = redis.call('GET', KEYS[1])
    
                    if not value then
                        return ''
                    end
    
                    local session = cjson.decode(value)
    
                    if session.tokenHash ~= ARGV[1] then
                        return ''
                    end
    
                    redis.call('DEL', KEYS[1])
    
                    local userSessionsKey =
                        'auth:refresh:user:' .. session.userId
    
                    redis.call(
                        'SREM',
                        userSessionsKey,
                        session.id
                    )
    
                    return value
                    """,
                    String.class
            );

    private static final DefaultRedisScript<Long> DELETE_ALL_BY_USER_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local sessionIds = redis.call('SMEMBERS', KEYS[1])
    
                    for _, sessionId in ipairs(sessionIds) do
                        redis.call(
                            'DEL',
                            'auth:refresh:' .. sessionId
                        )
                    end
    
                    redis.call('DEL', KEYS[1])
    
                    return #sessionIds
                    """,
                    Long.class
            );


    @Override
    public Optional<RefreshSession> consumeIfMatches(UUID sessionId, String tokenHash) {
        String value = redisTemplate.execute(
                CONSUME_SCRIPT,
                List.of(buildKey(sessionId)),
                tokenHash
                );

        if (value == null || value.isEmpty()){
            return Optional.empty();
        }

        RefreshSession session = objectMapper.readValue(value, RefreshSession.class);

        return Optional.of(session);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        redisTemplate.execute(DELETE_ALL_BY_USER_SCRIPT, List.of(buildUserSessionsKey(userId)));
    }

    private String buildKey(UUID sessionId){
        return KEY_PREFIX + sessionId;
    }

    private String buildUserSessionsKey(UUID userId){
        return USER_SESSIONS_KEY_PREFIX + userId;
    }
}
