package covy.covygoods.service;

import covy.covygoods.elastic.document.GoodsDocument;
import covy.covygoods.entity.GoodsEntity;
import java.util.List;

public interface GoodsService {

  Iterable<GoodsEntity> getAllGoods();

  Iterable<GoodsDocument> getgoods(String goodsNm);

  /* DB를 통한 상품 검색
  Iterable<GoodsEntity> getgoods(String goodsNm);*/

  List<String> getTopKeywords(int limit);

}
