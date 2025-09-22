package covy.covygoods.repository;

import covy.covygoods.elastic.document.GoodsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface GoodsSearchRepository extends ElasticsearchRepository<GoodsDocument, String> {
  List<GoodsDocument> findByGoodsNmContaining(String goodsNm); // like 검색
}