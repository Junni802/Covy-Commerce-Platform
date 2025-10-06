package covy.covygoods.service;

import covy.covygoods.elastic.document.GoodsDocument;
import covy.covygoods.entity.GoodsEntity;
import covy.covygoods.repository.GoodsRepository;
import covy.covygoods.repository.GoodsSearchRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <클래스 설명>
 *
 * @author : junni802
 * @date : 2025-02-25
 */

@Data
@Slf4j
@Service
public class GoodsServiceImpl implements GoodsService {

  GoodsRepository goodsRepository;
  GoodsSearchRepository goodsSearchRepository;

  @Autowired
  public GoodsServiceImpl(GoodsRepository goodsRepository, GoodsSearchRepository goodsSearchRepository) {
    this.goodsRepository = goodsRepository;
    this.goodsSearchRepository = goodsSearchRepository;
  }

  @Override
  public Iterable<GoodsEntity> getAllGoods() {
    return goodsRepository.findAll();
  }

  @Override
  public Iterable<GoodsDocument> getgoods(String goodsNm) {
    return goodsSearchRepository.findByGoodsNmContaining(goodsNm);
  }

  /* DB를 통한 상품 검색
  @Override
  public Iterable<GoodsEntity> getgoods(String goodsNm) {
    return goodsRepository.findByGoodsNmContaining(goodsNm);
  }*/
}