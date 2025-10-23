package covy.covycart.feign;

import covy.covycart.goods.dto.GoodsResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "covy-goods")
public interface GoodsServiceClient {

  @GetMapping("/search-goodsCd/{goodsCd}")
  GoodsResponseDto getGoodsCd(@PathVariable("goodsCd") String goodsCd);

}

