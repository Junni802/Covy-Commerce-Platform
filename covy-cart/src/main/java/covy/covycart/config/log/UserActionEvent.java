package covy.covycart.config.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 사용자 행동 이벤트를 표현하는 DTO
 * <p>
 * Cart 서비스에서 발생하는 추가, 제거, 전체삭제 이벤트를 Kafka로 전송할 때 사용
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserActionEvent {

  private String userId;       // 이벤트를 발생시킨 사용자 ID
  private String goodsCd;      // 상품 코드
  private String goodsNm;      // 상품명
  private String category;     // 상품 카테고리
  private ActionType actionType; // ADD_TO_CART, REMOVE_FROM_CART, CLEAR_CART 등
  private Long timestamp;      // 이벤트 발생 시각 (밀리초)

}
