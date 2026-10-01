package br.com.dwnl.spicehub.identity.infrastructure.security.login;

import br.com.dwnl.spicehub.identity.application.port.LoginAttemptService;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisLoginAttemptService implements LoginAttemptService {

    private static final String KEY_PREFIX = "identity:login:attempts:";

    private static final DefaultRedisScript<Long> RECORD_FAILURE_SCRIPT =
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
    private final LoginAttemptProperties properties;

    @Override
    public boolean isBlocked(Email email, String clientIp) {
        String value = redisTemplate.opsForValue().get(key(email, clientIp));

        if (value == null){
            return false;
        }

        return Long.parseLong(value) >= properties.maxAttempts();
    }

    @Override
    public void recordFailure(Email email, String clientIp) {
        redisTemplate.execute(
                RECORD_FAILURE_SCRIPT,
                List.of(key(email, clientIp)),
                String.valueOf(properties.window().toMillis())
        );
    }

    @Override
    public void reset(Email email, String clientIp) {
        redisTemplate.delete(key(email, clientIp));
    }

    private String key(Email email, String clientIp){
        return KEY_PREFIX + email.value() + ":" + clientIp;
    }

}
