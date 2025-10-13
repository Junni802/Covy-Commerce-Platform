package covy.covygoods.service;

import covy.covygoods.elastic.document.GoodsDocument;
import covy.covygoods.entity.GoodsEntity;
import covy.covygoods.repository.GoodsRepository;
import covy.covygoods.repository.GoodsSearchRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    if (cached != null) {
      return cached;
    }


    Page<GoodsDocument> result = goodsSearchRepository.findByGoodsNmContaining(goodsNm, pageable);
    redisTemplate.opsForValue().set(cacheKey, result.getContent(), 30, TimeUnit.MINUTES);
    return result;
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