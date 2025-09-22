package covy.covygoods.service;

import covy.covygoods.elastic.document.GoodsDocument;
import covy.covygoods.entity.GoodsEntity;

public interface GoodsService {

  Iterable<GoodsEntity> getAllGoods();

  Iterable<GoodsDocument> getgoods(String goodsNm);

}
