package covy.covyuser.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * Redis를 이용한 Refresh Token 관리 Repository
 * - 토큰 저장, 조회, 삭제 기능 제공
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

  private final StringRedisTemplate redisTemplate;

  private static final String PREFIX = "refreshToken:";

  /**
   * Refresh Token 저장
   *
   * @param userId          사용자 ID
   * @param refreshToken    발급된 Refresh Token
   * @param expirationMillis 만료 시간(ms)
   */
  public void saveRefreshToken(String userId, String refreshToken, long expirationMillis) {
    redisTemplate.opsForValue().set(PREFIX + userId, refreshToken, Duration.ofMillis(expirationMillis));
  }

  /**
   * Refresh Token 조회
   *
   * @param userId 사용자 ID
   * @return Redis에 저장된 Refresh Token
   */
  public String getRefreshToken(String userId) {
    return redisTemplate.opsForValue().get(PREFIX + userId);
  }

  /**
   * Refresh Token 삭제
   *
   * @param userId 사용자 ID
   */
  public void deleteRefreshToken(String userId) {
    redisTemplate.delete(PREFIX + userId);
  }
}
