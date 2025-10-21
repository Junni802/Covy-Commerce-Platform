package covy.covycart.controller;

import covy.covycart.dto.CartRequest;
import covy.covycart.dto.CartResponse;
import covy.covycart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
  private final CartService cartService;

  @PostMapping
  public CartResponse addItem(
      @RequestHeader("X-User-Id") String userId,
      @RequestBody CartRequest request) {
    return cartService.addItem(userId, request);
  }

  @GetMapping
  public CartResponse getCart(@RequestHeader("X-User-Id") String userId) {
    return cartService.getCart(userId);
  }

  @DeleteMapping("/{productId}")
  public void removeItem(
      @RequestHeader("X-User-Id") String userId,
      @PathVariable String productId) {
    cartService.removeItem(userId, productId);
  }
}
