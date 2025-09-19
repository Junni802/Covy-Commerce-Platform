package covy.covygoods.service;

import covy.covygoods.entity.GoodsEntity;

public interface GoodsService {

  Iterable<GoodsEntity> getAllGoods();

  Iterable<GoodsEntity> getgoods(String goodsNm);

}
