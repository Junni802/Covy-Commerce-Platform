package covy.covyuser.user.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RefreshTokenRedisRepositoryTest {

  @Autowired
  private RefreshTokenRedisRepository refreshTokenRedisRepository;

  @Autowired
  private StringRedisTemplate redisTemplate;

  private static final String USER_ID = "test1234";
  private static final String REFRESH_TOKEN = "test1234"; // as per your example

  @AfterEach
  void cleanup() {
    // 테스트 후 정리 — 남은 키 삭제
    refreshTokenRedisRepository.deleteRefreshToken(USER_ID);
  }

  @Test
  @DisplayName("Refresh Token 저장 후 조회가 가능해야 한다")
  void saveAndGetRefreshToken() {
    // given
    refreshTokenRedisRepository.saveRefreshToken(USER_ID, REFRESH_TOKEN, 60_000); // 만료 1분

    // when
    String tokenInRedis = refreshTokenRedisRepository.getRefreshToken(USER_ID);

    System.out.println("값 보자 -> " + tokenInRedis);

    // then
    assertThat(tokenInRedis).isEqualTo(REFRESH_TOKEN);
  }

  @Test
  @DisplayName("Refresh Token 삭제 후 조회하면 null 이어야 한다")
  void deleteRefreshToken_thenGetShouldReturnNull() {
    // given
    refreshTokenRedisRepository.saveRefreshToken(USER_ID, REFRESH_TOKEN, 60_000);
    assertThat(refreshTokenRedisRepository.getRefreshToken(USER_ID)).isEqualTo(REFRESH_TOKEN);

    // when
    refreshTokenRedisRepository.deleteRefreshToken(USER_ID);

    // then
    String tokenInRedis = refreshTokenRedisRepository.getRefreshToken(USER_ID);
    assertThat(tokenInRedis).isNull();
  }

  @Test
  @DisplayName("만료 시간 초과 후 조회하면 null 이어야 한다")
  void expiredRefreshTokenShouldNotBeRetrieved() throws InterruptedException {
    // given: very short expiration
    refreshTokenRedisRepository.saveRefreshToken(USER_ID, REFRESH_TOKEN, 500L); // 0.5초

    // when
    Thread.sleep(1_000L); // 1초 대기 (토큰 만료)

    // then
    String tokenInRedis = refreshTokenRedisRepository.getRefreshToken(USER_ID);
    assertThat(tokenInRedis).isNull();
  }
}
