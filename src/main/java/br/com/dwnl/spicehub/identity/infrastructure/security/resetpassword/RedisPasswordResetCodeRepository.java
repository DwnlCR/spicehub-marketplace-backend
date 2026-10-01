package br.com.dwnl.spicehub.identity.infrastructure.security.resetpassword;

import br.com.dwnl.spicehub.identity.domain.model.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisPasswordResetCodeRepository implements PasswordResetCodeRepository {

    private static final String CODE_KEY_PREFIX = "identity:password-reset:code:";

    private static final String COOLDOWN_KEY_PREFIX = "identity:password-reset:cooldown:";

    private static  final String ATTEMPTS_KEY_PREFIX = "identity:password-reset:attempts:";

    private static final DefaultRedisScript<Long> SAVE_SCRIPT =
            new DefaultRedisScript<>(
            """
            redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2])
            redis.call('DEL', KEYS[2])
            return 1
            """, Long.class
    );

    private static final DefaultRedisScript<Long> CONSUME_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local storedHash = redis.call('GET', KEYS[1])

                    if not storedHash then
                        return 0
                    end
                    
                    local attempts = tonumber(redis.call('GET', KEYS[2]) or '0')
                    local maxAttempts = tonumber(ARGV[2])
                    
                    if attempts >= maxAttempts then
                        return 0
                    end

                    if storedHash ~= ARGV[1] then
                        attempts = redis.call('INCR', KEYS[2])
                        
                        if attempts == 1 then
                            local ttl = redis.call('PTTL', KEYS[1])
                            
                            if ttl > 0 then 
                                redis.call('PEXPIRE', KEYS[2], ttl)
                            end
                        end
                        return 0    
                    end
                    
                    redis.call('DEL', KEYS[1], KEYS[2])
                    return 1
                    """,
                    Long.class
            );

    private static final DefaultRedisScript<Long> DELETE_CODE_AND_COOLDOWN_SCRIPT =
            new DefaultRedisScript<>("""
            return redis.call('DEL', KEYS[1], KEYS[2], KEYS[3])
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(Email email, String codeHash, Duration ttl) {
        redisTemplate.execute(
                SAVE_SCRIPT,
                List.of(
                        codeKey(email),
                        attemptsKey(email)
                ),
                codeHash,
                String.valueOf(ttl.toMillis())
        );
    }

    @Override
    public boolean consumeIfMatches(Email email, String codeHash, int maxAttempts) {
        Long result = redisTemplate.execute(
                CONSUME_SCRIPT,
                List.of(
                        codeKey(email),
                        attemptsKey(email)
                ),
                codeHash,
                String.valueOf(maxAttempts)
                );

        return Long.valueOf(1L).equals(result);
    }

    @Override
    public boolean acquireCooldown(Email email, Duration ttl) {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(
                        cooldownKey(email),
                        "1",
                        ttl
                );

        return Boolean.TRUE.equals(acquired);
    }

    @Override
    public void deleteCodeAndCooldown(Email email) {
        redisTemplate.execute(DELETE_CODE_AND_COOLDOWN_SCRIPT, List.of(codeKey(email), cooldownKey(email), attemptsKey(email)));
    }

    private String codeKey(Email email) {
        return CODE_KEY_PREFIX + email.value();
    }

    private String cooldownKey(Email email) {
        return COOLDOWN_KEY_PREFIX + email.value();
    }

    private String attemptsKey(Email email){
        return ATTEMPTS_KEY_PREFIX + email.value();
    }
}