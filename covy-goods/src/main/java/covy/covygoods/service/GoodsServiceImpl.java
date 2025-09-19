package covy.covygoods.service;

import covy.covygoods.entity.GoodsEntity;
import covy.covygoods.repository.GoodsRepository;
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

  @Autowired
  public GoodsServiceImpl(GoodsRepository goodsRepository) {
    this.goodsRepository = goodsRepository;
  }

  @Override
  public Iterable<GoodsEntity> getAllGoods() {
    return goodsRepository.findAll();
  }

  @Override
  public Iterable<GoodsEntity> getgoods(String goodsNm) {
    return goodsRepository.findByGoodsNmContaining(goodsNm);
  }
}