package br.com.dwnl.spicehub.identity.infrastructure.security.registration;

import br.com.dwnl.spicehub.identity.application.port.RegistrationAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisRegistrationAttemptService implements RegistrationAttemptService {

    private static final String KEY_PREFIX = "identity:registration:attempts:";

    private static final DefaultRedisScript<Long> RECORD_ATTEMPT_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local attempts = redis.call('INCR', KEYS[1])
                    
                    if attempts == 1 then
                        redis.call('PEXPIRE', KEYS[1], ARGV[1])
                    end
                    
                    return attempts
                    """, Long.class
            );

    private final StringRedisTemplate redisTemplate;
    private final RegistrationAttemptProperties attemptProperties;

    @Override
    public boolean isBlocked(String clientIp) {
        String value = redisTemplate.opsForValue().get(key(clientIp));

        if (value == null){
            return false;
        }

        return Long.parseLong(value) >= attemptProperties.maxAttempts();
    }

    @Override
    public void recordAttempt(String clientIp) {
        redisTemplate.execute(
          RECORD_ATTEMPT_SCRIPT,
          List.of(key(clientIp)),
          String.valueOf(attemptProperties.window().toMillis())
        );
    }

    private String key(String clientIp){
        return KEY_PREFIX + clientIp;
    }
}
