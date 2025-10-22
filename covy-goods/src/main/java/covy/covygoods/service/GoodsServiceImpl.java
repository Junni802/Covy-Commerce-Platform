package covy.covygoods.service;

import covy.covygoods.common.elastic.document.GoodsDocument;
import covy.covygoods.dto.GoodsDto;
import covy.covygoods.entity.GoodsEntity;
import covy.covygoods.repository.GoodsRepository;
import covy.covygoods.repository.GoodsSearchRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
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
  RedisTemplate<String, Object> redisTemplate;
  StringRedisTemplate stringRedisTemplate;

  @Autowired
  public GoodsServiceImpl(GoodsRepository goodsRepository, GoodsSearchRepository goodsSearchRepository
    , RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
    this.goodsRepository = goodsRepository;
    this.goodsSearchRepository = goodsSearchRepository;
    this.redisTemplate = redisTemplate;
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Override
  public Iterable<GoodsEntity> getAllGoods() {
    return goodsRepository.findAll();
  }

  @Override
  public Iterable<GoodsDocument> getgoods(String goodsNm, Pageable pageable) {
    String cacheKey = "search:" + goodsNm;
    List<GoodsDocument> cached = (List<GoodsDocument>)  redisTemplate.opsForValue().get(cacheKey);

    // ✅ 1. 검색어 인기 카운트 증가
    stringRedisTemplate.opsForZSet().incrementScore("popular:keywords", goodsNm, 1);

    if (cached != null) {
      return cached;
    }

    Page<GoodsDocument> result = goodsSearchRepository.findByGoodsNmContaining(goodsNm, pageable);
    redisTemplate.opsForValue().set(cacheKey, result.getContent(), 30, TimeUnit.MINUTES);
    return result;
  }

  @Override
  public Optional<GoodsEntity> getGoodsCd(String goodsCd) {
    return goodsRepository.findByGoodsCd(goodsCd);
  }

  public GoodsDocument saveGoods(GoodsDto goods) {
    // 중복 확인
    goodsRepository.findByGoodsCd(goods.getGoodsCd())
        .ifPresent(existing -> {
          throw new IllegalArgumentException("이미 존재하는 상품 코드입니다: " + goods.getGoodsCd());
        });

    return goodsRepository.saveGoods(goods);
  }

  /* DB를 통한 상품 검색
  @Override
  public Iterable<GoodsEntity> getgoods(String goodsNm) {
    return goodsRepository.findByGoodsNmContaining(goodsNm);
  }*/

  @Override
  public List<String> getTopKeywords(int limit) {
    String redisKey = "popular:keywords";

    Set<String> topKeywords = stringRedisTemplate.opsForZSet()
        .reverseRange(redisKey, 0, limit - 1);

    return new ArrayList<>(topKeywords);
  }
}