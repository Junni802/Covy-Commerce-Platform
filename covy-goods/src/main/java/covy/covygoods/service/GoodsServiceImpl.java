package covy.covygoods.service;

import covy.covygoods.common.elastic.document.GoodsDocument;
import covy.covygoods.entity.GoodsEntity;
import covy.covygoods.repository.GoodsRepository;
import covy.covygoods.repository.GoodsSearchRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoodsServiceImpl implements GoodsService {

  private final GoodsRepository goodsRepository;
  private final GoodsSearchRepository goodsSearchRepository;
  private final RedisTemplate<String, Object> redisTemplate;
  private final StringRedisTemplate stringRedisTemplate;

  @Autowired
  public GoodsServiceImpl(
      GoodsRepository goodsRepository,
      GoodsSearchRepository goodsSearchRepository,
      RedisTemplate<String, Object> redisTemplate,
      StringRedisTemplate stringRedisTemplate
  ) {
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
    List<GoodsDocument> cached = (List<GoodsDocument>) redisTemplate.opsForValue().get(cacheKey);

    // 검색어 인기 카운트 증가
    stringRedisTemplate.opsForZSet().incrementScore("popular:keywords", goodsNm, 1);

    if (cached != null) {
      return cached;
    }

    Page<GoodsDocument> result = goodsSearchRepository.findByGoodsNmContaining(goodsNm, pageable);
    redisTemplate.opsForValue().set(cacheKey, result.getContent(), 30, TimeUnit.MINUTES);
    return result.getContent();
  }

  @Override
  public Optional<GoodsEntity> getGoodsCd(String goodsCd) {
    return goodsRepository.findByGoodsCd(goodsCd);
  }

  @Override
  public Iterable<GoodsEntity> saveGoodsAll(Iterable<GoodsEntity> goodsEntityList) {
    return goodsRepository.saveAll(goodsEntityList);
  }

  @Override
  public GoodsDocument saveGoods(GoodsEntity goods) {
    // 중복 확인
    goodsRepository.findByGoodsCd(goods.getGoodsCd())
        .ifPresent(existing -> {
          throw new IllegalArgumentException("이미 존재하는 상품 코드입니다: " + goods.getGoodsCd());
        });

    // RDB 저장
    goodsRepository.save(goods);

    // Elasticsearch 저장
    GoodsDocument doc = new GoodsDocument();
    doc.setGoodsCd(goods.getGoodsCd());
    doc.setGoodsNm(goods.getGoodsNm());
    doc.setCategory(goods.getCategory());
    doc.setPrice(goods.getPrice());
    doc.setDeleted(goods.isDeleted()); // boolean 필드

    return goodsSearchRepository.save(doc);
  }

  @Override
  public List<String> getTopKeywords(int limit) {
    String redisKey = "popular:keywords";

    Set<String> topKeywords = stringRedisTemplate.opsForZSet()
        .reverseRange(redisKey, 0, limit - 1);

    return new ArrayList<>(topKeywords);
  }
}
