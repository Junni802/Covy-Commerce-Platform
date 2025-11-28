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

  private static final String POPULAR_KEYWORDS = "popular:keywords";
  private static final String SEARCH_KEY_PREFIX = "search:";

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

  /**
   * 전체 상품 목록을 조회한다.
   *
   * @return 모든 GoodsEntity 목록
   */
  @Override
  public Iterable<GoodsEntity> getAllGoods() {
    return goodsRepository.findAll();
  }

  /**
   * 상품명 검색을 수행한다.
   * - Redis 캐시 우선 조회
   * - 검색 시 인기 검색어 카운트 증가
   * - 캐시 미스 시 Elasticsearch 검색 후 캐시 저장
   *
   * @param goodsNm 검색어
   * @param pageable 페이징 정보
   * @return 검색된 GoodsDocument 리스트
   */
  @Override
  public Iterable<GoodsDocument> getgoods(String goodsNm, Pageable pageable) {

    String cacheKey = SEARCH_KEY_PREFIX + goodsNm;

    // 인기 검색어 카운트 증가
    stringRedisTemplate.opsForZSet().incrementScore(POPULAR_KEYWORDS, goodsNm, 1);

    // 캐시 체크
    List<GoodsDocument> cached = getCachedGoods(cacheKey);
    if (cached != null) {
      log.debug("Cache hit for goodsNm={}", goodsNm);
      return cached;
    }

    // Elasticsearch 검색
    Page<GoodsDocument> result =
        goodsSearchRepository.findByGoodsNmContaining(goodsNm, pageable);

    // 검색 결과 캐시 저장 (30분)
    cacheGoods(cacheKey, result.getContent());

    return result.getContent();
  }

  /**
   * 상품 코드로 RDB 내 상품을 조회한다.
   *
   * @param goodsCd 상품 코드
   * @return Optional<GoodsEntity>
   */
  @Override
  public Optional<GoodsEntity> getGoodsCd(String goodsCd) {
    return goodsRepository.findByGoodsCd(goodsCd);
  }

  /**
   * 여러 상품을 일괄 저장한다.
   *
   * @param goodsEntityList 상품 엔티티 리스트
   * @return 저장된 엔티티 목록
   */
  @Override
  public Iterable<GoodsEntity> saveGoodsAll(Iterable<GoodsEntity> goodsEntityList) {
    return goodsRepository.saveAll(goodsEntityList);
  }

  /**
   * 단일 상품을 저장한다.
   * - RDB 중복 체크
   * - RDB 저장
   * - Elasticsearch 저장
   *
   * @param goods GoodsEntity
   * @return 저장된 GoodsDocument
   */
  @Override
  public GoodsDocument saveGoods(GoodsEntity goods) {

    // 중복 체크
    if (goodsRepository.findByGoodsCd(goods.getGoodsCd()).isPresent()) {
      throw new IllegalArgumentException("이미 존재하는 상품 코드입니다: " + goods.getGoodsCd());
    }

    // RDB 저장
    goodsRepository.save(goods);

    // ES Document 변환
    GoodsDocument doc = convertToDocument(goods);

    // ES 저장
    return goodsSearchRepository.save(doc);
  }

  /**
   * 인기 검색어 상위 N개를 조회한다.
   *
   * @param limit 조회할 키워드 수
   * @return 키워드 리스트
   */
  @Override
  public List<String> getTopKeywords(int limit) {
    Set<String> topKeywords = stringRedisTemplate.opsForZSet()
        .reverseRange(POPULAR_KEYWORDS, 0, limit - 1);

    return new ArrayList<>(topKeywords);
  }

  // ---------------------- Private Methods ----------------------

  /**
   * Redis 캐시에서 검색 결과 조회
   */
  private List<GoodsDocument> getCachedGoods(String cacheKey) {
    Object cached = redisTemplate.opsForValue().get(cacheKey);
    if (cached instanceof List<?>) {
      return (List<GoodsDocument>) cached;
    }
    return null;
  }

  /**
   * Redis에 검색 결과 저장 (30분 TTL)
   */
  private void cacheGoods(String cacheKey, List<GoodsDocument> documents) {
    redisTemplate.opsForValue().set(cacheKey, documents, 30, TimeUnit.MINUTES);
  }

  /**
   * GoodsEntity → GoodsDocument 변환
   */
  private GoodsDocument convertToDocument(GoodsEntity goods) {
    GoodsDocument doc = new GoodsDocument();
    doc.setGoodsCd(goods.getGoodsCd());
    doc.setGoodsNm(goods.getGoodsNm());
    doc.setCategory(goods.getCategory());
    doc.setPrice(goods.getPrice());
    doc.setDeleted(goods.isDeleted());
    return doc;
  }
}
