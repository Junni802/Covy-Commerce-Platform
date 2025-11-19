package covy.covycart.feign;

import covy.covycart.goods.dto.GoodsResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * GoodsService 와 통신하는 Feign Client.
 * 상품 조회 관련 기능을 담당한다.
 *
 * - name : 서비스 디스커버리(eureka)에서 등록된 서비스명
 * - path  : 공통 Prefix 지정으로 중복 제거
 */
@FeignClient(
    name = "covy-goods",
    path = "/goods"      // 모든 API 앞에 공통 prefix 적용
)
public interface GoodsServiceClient {

  /**
   * 상품코드로 단일 상품 조회 API
   *
   * @param goodsCd 상품코드
   * @return GoodsResponseDto (존재하지 않으면 null 반환 가능)
   */
  @GetMapping("/search-goodsCd/{goodsCd}")
  GoodsResponseDto getGoodsByCode(@PathVariable("goodsCd") String goodsCd);
}
