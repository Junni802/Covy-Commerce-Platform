package covy.covygoods.service;

import covy.covygoods.entity.GoodsEntity;

public interface GoodsService {

  Iterable<GoodsEntity> getAllCatalogs();

}
