package covy.covycart.service;

import covy.covycart.domain.Cart;
import covy.covycart.domain.CartItem;
import covy.covycart.dto.CartRequest;
import covy.covycart.dto.CartResponse;
import covy.covycart.repository.CartRepository;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {
  private final CartRepository cartRepository;

  public CartResponse addItem(String userId, CartRequest request) {
    Cart cart = cartRepository.findByUserId(userId);
    if (cart == null) cart = new Cart(userId, new ArrayList<>());

    // 기존 아이템 있으면 수량 증가
    Optional<CartItem> existingItem = cart.getItems().stream()
        .filter(i -> i.getProductId().equals(request.getProductId()))
        .findFirst();
    if (existingItem.isPresent()) {
      existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
    } else {
      cart.getItems().add(new CartItem(request.getProductId(), request.getQuantity()));
    }

    cartRepository.save(cart);
    return new CartResponse(cart.getUserId(), cart.getItems());
  }

  public CartResponse getCart(String userId) {
    Cart cart = cartRepository.findByUserId(userId);
    if (cart == null) return new CartResponse(userId, new ArrayList<>());
    return new CartResponse(cart.getUserId(), cart.getItems());
  }

  public void removeItem(String userId, String productId) {
    Cart cart = cartRepository.findByUserId(userId);
    if (cart != null) {
      cart.getItems().removeIf(i -> i.getProductId().equals(productId));
      cartRepository.save(cart);
    }
  }
}
