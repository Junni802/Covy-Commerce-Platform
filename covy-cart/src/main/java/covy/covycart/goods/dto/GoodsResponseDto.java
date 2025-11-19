package covy.covycart.goods.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 상품 조회 응답 DTO
 * <p>
 * del_yn 필드를 boolean 타입 deleted로 매핑하여 표현
 */
@Getter
@ToString
@Builder
public class GoodsResponseDto {
  private final String goodsCd;
  private final String goodsNm;
  private final String category;
  private final Integer price;
  private final boolean deleted;
}
