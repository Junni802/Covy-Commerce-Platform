package covy.covygoods.controller;

import covy.covygoods.entity.GoodsEntity;
import covy.covygoods.repository.CatalogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CatalogControllerTest {

  @Autowired
  private CatalogRepository catalogRepository;

  @Test
  void test() {
    for (int i = 1; i <= 10000; i++) {
      GoodsEntity entity = new GoodsEntity();
      entity.setGoodsCd("P-" + i); // 고유 productId (중복 방지)
      entity.setGoodsNm("초코에몽" + i);
      entity.setStock(100); // 초기 재고 임의 값
      entity.setUnitPrice(1500); // 임의 가격
      catalogRepository.save(entity);
    }
  }

}