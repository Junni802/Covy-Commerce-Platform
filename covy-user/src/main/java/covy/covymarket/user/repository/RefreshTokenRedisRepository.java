package covy.covymarket.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

  private final StringRedisTemplate redisTemplate;

  private static final String PREFIX = "refreshToken:";

  public void saveRefreshToken(String userId, String refreshToken, long expirationMillis) {
    redisTemplate.opsForValue().set(PREFIX + userId, refreshToken, Duration.ofMillis(expirationMillis));
  }

  public String getRefreshToken(String userId) {
    return redisTemplate.opsForValue().get(PREFIX + userId);
  }

  public void deleteRefreshToken(String userId) {
    redisTemplate.delete(PREFIX + userId);
  }
}