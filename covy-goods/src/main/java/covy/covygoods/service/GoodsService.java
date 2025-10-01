package covy.covygoods.service;

import covy.covygoods.elastic.document.GoodsDocument;
import covy.covygoods.entity.GoodsEntity;
import java.util.List;

public interface GoodsService {

  Iterable<GoodsEntity> getAllGoods();

  List<GoodsDocument> getgoods(String goodsNm);

}
