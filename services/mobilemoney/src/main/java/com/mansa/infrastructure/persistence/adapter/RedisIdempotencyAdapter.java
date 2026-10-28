package com.mansa.infrastructure.persistence.adapter;



import com.mansa.application.port.out.IdempotencyPort;
import com.mansa.domain.valueobject.IdempotencyKey;
import com.mansa.domain.valueobject.TransactionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisIdempotencyAdapter implements IdempotencyPort {

    private static final String KEY_PREFIX = "idempotency:mobilemoney:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public Optional<TransactionId> getIfPresent(IdempotencyKey key) {
        String redisKey = buildRedisKey(key);
        String value = redisTemplate.opsForValue().get(redisKey);
        if (value == null) {
            return Optional.empty();
        }
        log.debug("Idempotency key hit: key={}, transactionId={}", key, value);
        return Optional.of(TransactionId.of(value));
    }

    @Override
    public void store(IdempotencyKey key, TransactionId transactionId, long ttlSeconds) {
        String redisKey = buildRedisKey(key);
        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(
                redisKey,
                transactionId.toString(),
                Duration.ofSeconds(ttlSeconds)
        );
        if (Boolean.FALSE.equals(wasSet)) {
            log.warn("Idempotency key already exists, not overwriting: key={}", key);
        } else {
            log.debug("Idempotency key stored: key={}, transactionId={}, ttl={}s",
                    key, transactionId, ttlSeconds);
        }
    }

    @Override
    public void evict(IdempotencyKey key) {
        Boolean deleted = redisTemplate.delete(buildRedisKey(key));
        log.debug("Idempotency key evicted: key={}, deleted={}", key, deleted);
    }

    private String buildRedisKey(IdempotencyKey key) {
        return KEY_PREFIX + key.value();
    }
}
