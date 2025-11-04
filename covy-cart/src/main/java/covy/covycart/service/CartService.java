package covy.covycart.service;

import covy.covycart.config.log.ActionType;
import covy.covycart.config.log.UserActionEvent;
import covy.covycart.domain.Cart;
import covy.covycart.domain.CartItem;
import covy.covycart.dto.CartRequest;
import covy.covycart.dto.CartResponse;
import covy.covycart.repository.CartRepository;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {
  private final CartRepository cartRepository;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final RedisTemplate<String, Object> redisTemplate;

  public void addItem(String userId, CartRequest request) {
    UserActionEvent event = new UserActionEvent(userId, request.getGoodsCd(), ActionType.ADD_TO_CATRT, System.currentTimeMillis());
    if (event.getActionType() == ActionType.ADD_TO_CATRT) {
      // Redis에 추가

    } else if (event.getActionType() == ActionType.REMOVE_FROM_CART) {
      // Redis에서 제거
    } else if (event.getActionType() == ActionType.CLEAR_CART) {
      // 전체 삭제
    }

    kafkaTemplate.send("cart-events", userId, event.toString());
  }

  public CartResponse getCart(String userId) {
    Cart cart = cartRepository.findByUserId(userId);
    if (cart == null) return new CartResponse(userId, new ArrayList<>());
    return new CartResponse(cart.getUserId(), cart.getItems());
  }

  public void removeItem(String userId, String productId) {
    Cart cart = cartRepository.findByUserId(userId);
    if (cart != null) {
      cart.getItems().removeIf(i -> i.getGoodsCd().equals(productId));
      cartRepository.save(cart);
    }
  }
}
