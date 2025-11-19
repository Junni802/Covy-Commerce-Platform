package covy.covycart.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covycart.config.log.ActionType;
import covy.covycart.config.log.UserActionEvent;
import covy.covycart.domain.Cart;
import covy.covycart.dto.CartRequest;
import covy.covycart.dto.CartResponse;
import covy.covycart.feign.GoodsServiceClient;
import covy.covycart.goods.dto.GoodsResponseDto;
import covy.covycart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;
  private final GoodsServiceClient goodsServiceClient;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper(); // 재사용 가능하도록 필드로 분리

  /**
   * 장바구니에 아이템 추가
   * 1) 외부 상품 서비스에서 상품 정보 조회
   * 2) 조회 실패 시 처리하지 않고 종료
   * 3) 이벤트 생성 후 Redis/Kafka 처리
   */
  public void addItem(String userId, CartRequest request) throws JsonProcessingException {
    GoodsResponseDto goods = goodsServiceClient.getGoodsByCode(request.getGoodsCd());

    if (ObjectUtils.isEmpty(goods)) {
      // 상품 정보가 없으면 장바구니 처리하지 않음
      return;
    }

    UserActionEvent event = buildAddEvent(userId, goods);

    // Redis 캐싱 처리
    handleRedis(event);

    // Kafka 이벤트 발행
    kafkaTemplate.send("cart-events", userId, objectMapper.writeValueAsString(event));
  }

  /**
   * 장바구니 조회
   */
  public CartResponse getCart(String userId) {
    Cart cart = cartRepository.findByUserId(userId);
    if (cart == null) {
      return new CartResponse(userId, new ArrayList<>());
    }
    return new CartResponse(cart.getUserId(), cart.getItems());
  }

  /**
   * 장바구니 특정 아이템 제거
   */
  public void removeItem(String userId, String productId) {
    Cart cart = cartRepository.findByUserId(userId);
    if (cart != null) {
      cart.getItems().removeIf(item -> item.getGoodsCd().equals(productId));
      cartRepository.save(cart);
    }
  }

  // ================================
  // Private helper methods
  // ================================

  /**
   * UserActionEvent 빌드 (Builder 사용)
   */
  private UserActionEvent buildAddEvent(String userId, GoodsResponseDto goods) {
    return UserActionEvent.builder()
        .userId(userId)
        .goodsCd(goods.getGoodsCd())
        .goodsNm(goods.getGoodsNm())
        .category(goods.getCategory())
        .actionType(ActionType.ADD_TO_CATRT)
        .timestamp(System.currentTimeMillis())
        .build();
  }

  /**
   * Redis 처리 로직
   * 실제 Redis 키 전략과 TTL 설정을 여기서 적용 가능
   */
  private void handleRedis(UserActionEvent event) {
    switch (event.getActionType()) {
      case ADD_TO_CATRT:
        // TODO: Redis에 추가
        break;
      case REMOVE_FROM_CART:
        // TODO: Redis에서 제거
        break;
      case CLEAR_CART:
        // TODO: Redis 전체 삭제
        break;
      default:
        // 예외 처리 가능 (로그 등)
        break;
    }
  }
}
