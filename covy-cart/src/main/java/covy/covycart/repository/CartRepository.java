package covy.covycart.repository;

import covy.covycart.domain.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis를 이용한 Cart 저장소
 * <p>
 * key는 "cart:{userId}" 형태로 관리
 */
@Repository
@RequiredArgsConstructor
public class CartRepository {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String CART_KEY_PREFIX = "cart:";

  /**
   * 장바구니 저장 또는 업데이트
   */
  public void save(Cart cart) {
    redisTemplate.opsForValue().set(getKey(cart.getUserId()), cart);
  }

  /**
   * 사용자 ID로 장바구니 조회
   */
  public Cart findByUserId(String userId) {
    return (Cart) redisTemplate.opsForValue().get(getKey(userId));
  }

  /**
   * 사용자 장바구니 삭제
   */
  public void delete(String userId) {
    redisTemplate.delete(getKey(userId));
  }

  /**
   * Redis key 생성
   */
  private String getKey(String userId) {
    return CART_KEY_PREFIX + userId;
  }
}
