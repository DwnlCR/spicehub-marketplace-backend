package br.com.dwnl.spicehub.identity.infrastructure.security.emailverification;

import br.com.dwnl.spicehub.identity.application.port.EmailVerificationCodeRepository;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisEmailVerificationCodeRepository implements EmailVerificationCodeRepository {

    private static final String KEY_PREFIX = "email-verification:";
    private static final String COOLDOWN_KEY_PREFIX = "email-verification:cooldown:";

    private static final DefaultRedisScript<Long> CONSUME_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local storedHash = redis.call('GET', KEYS[1])

                    if not storedHash then
                        return 0
                    end

                    if storedHash ~= ARGV[1] then
                        return 0
                    end

                    redis.call('DEL', KEYS[1])

                    return 1
                    """,
                    Long.class
            );

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(Email email, String codeHash, Duration codeTtl) {
        redisTemplate.opsForValue().set(
                key(email),
                codeHash,
                codeTtl
        );
    }

    @Override
    public boolean consumeIfMatches(Email email, String codeHash) {
        Long result = redisTemplate.execute(
                CONSUME_SCRIPT,
                List.of(key(email)),
                codeHash
        );

        return Long.valueOf(1L).equals(result);
    }

    @Override
    public boolean acquireResendCooldown(Email email, Duration ttl) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                cooldownKey(email),
                "1",
                ttl
        );

        return Boolean.TRUE.equals(acquired);
    }

    private String key(Email email){
        return KEY_PREFIX + email.value();
    }

    private String cooldownKey(Email email) {
        return COOLDOWN_KEY_PREFIX + email.value();
    }
}
