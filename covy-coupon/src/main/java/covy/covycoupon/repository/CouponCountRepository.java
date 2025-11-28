package covy.covycoupon.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponCountRepository {

  private static final Logger log = LoggerFactory.getLogger(CouponCountRepository.class);

  private final RedisTemplate<String, String> redisTemplate;
  
  @Qualifier("limitedCouponScript")
  private final RedisScript<Long> redisScript;

  private static final String COUPON_USER_SET_KEY = "coupon-users";
  private static final String COUPON_COUNT_KEY = "coupon-count";

  /**
   * Lua 기반 쿠폰 발급 처리
   *
   * 동시성 이슈 처리 가능
   * 순서 이슈 처리 불가능
   *
   * @param userId 사용자 ID
   * @return 발급 성공 여부
   */
  public boolean issueCoupon(Long userId) {
    List<String> keys = List.of(
        COUPON_USER_SET_KEY,
        COUPON_COUNT_KEY
    );

    Long result = redisTemplate.execute(
        redisScript,
        keys,
        String.valueOf(userId)
    );

    if (result == null) {
      return false;
    }

    return switch (result.intValue()) {
      case 0 -> {
        log.info("이미 발급 받은 사용자입니다. userId={}", userId);
        yield false;
      }
      case -1 -> {
        log.info("쿠폰 수량이 초과되었습니다. userId={}", userId);
        yield false;
      }
      case 1 -> true;
      default -> false;
    };
  }

  /**
   * 테스트용: Redis Key 삭제
   */
  public void deleteKey(String key) {
    redisTemplate.delete(key);
  }
}
