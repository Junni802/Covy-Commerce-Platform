package covy.covygoods.repository;

import covy.covygoods.elastic.document.GoodsDocument;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsSearchRepository extends ElasticsearchRepository<GoodsDocument, String> {
  List<GoodsDocument> findByGoodsNmContaining(String goodsNm);
}