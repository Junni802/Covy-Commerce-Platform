package covy.covycart.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import covy.covycart.dto.CartRequest;
import covy.covycart.dto.CartResponse;
import covy.covycart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  /**
   * 장바구니에 아이템 추가
   *
   * @param userId 사용자 ID (헤더 X-User-Id)
   * @param request 장바구니에 추가할 상품 정보
   * @return 성공 여부
   */
  @PostMapping
  public ResponseEntity<Boolean> addItem(
      @RequestHeader("X-User-Id") String userId,
      @RequestBody CartRequest request) throws JsonProcessingException {

      cartService.addItem(userId, request);
      return ResponseEntity.ok(true);
  }

  /**
   * 사용자 장바구니 조회
   *
   * @param userId 사용자 ID (헤더 X-User-Id)
   * @return CartResponse 장바구니 정보
   */
  @GetMapping
  public ResponseEntity<CartResponse> getCart(@RequestHeader("X-User-Id") String userId) {
    CartResponse cart = cartService.getCart(userId);
    return ResponseEntity.ok(cart);
  }

  /**
   * 장바구니 아이템 삭제
   *
   * @param userId 사용자 ID (헤더 X-User-Id)
   * @param productId 삭제할 상품 코드
   */
  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> removeItem(
      @RequestHeader("X-User-Id") String userId,
      @PathVariable String productId) {

    cartService.removeItem(userId, productId);
    return ResponseEntity.noContent().build();
  }
}
