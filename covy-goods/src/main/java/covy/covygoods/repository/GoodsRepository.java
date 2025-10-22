package covy.covygoods.repository;

import covy.covygoods.common.elastic.document.GoodsDocument;
import covy.covygoods.dto.GoodsDto;
import covy.covygoods.entity.GoodsEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

/**
 * <클래스 설명>
 *
 * @author : junni802
 * @date : 2025-02-25
 */
public interface GoodsRepository extends CrudRepository<GoodsEntity, Long> {
  Optional<GoodsEntity> findByGoodsCd(String goodsCd);

  List<GoodsEntity> findByGoodsNmContaining(String goodsNm);
}