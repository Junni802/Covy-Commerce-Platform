package covy.covygoods.repository;

import covy.covygoods.elastic.document.GoodsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface GoodsSearchRepository extends ElasticsearchRepository<GoodsDocument, String> {
  Iterable<GoodsDocument> findByGoodsNmContaining(String goodsNm);
}