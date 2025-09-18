package covy.covygoods.service;

import covy.covygoods.entity.GoodsEntity;

public interface CatalogService {

  Iterable<GoodsEntity> getAllCatalogs();

}
