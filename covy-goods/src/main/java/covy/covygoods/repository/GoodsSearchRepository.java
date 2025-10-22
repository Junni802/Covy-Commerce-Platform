package covy.covygoods.repository;

import covy.covygoods.common.elastic.document.GoodsDocument;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsSearchRepository extends ElasticsearchRepository<GoodsDocument, String> {
  Page<GoodsDocument> findByGoodsNmContaining(String goodsNm, Pageable pageable);
  Optional<GoodsDocument> findByGoodsCd(String goodsCd);
}