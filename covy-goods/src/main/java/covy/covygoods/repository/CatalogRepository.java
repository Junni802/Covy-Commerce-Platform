package covy.covygoods.repository;

import covy.covygoods.entity.CatalogEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * <클래스 설명>
 *
 * @author : junni802
 * @date : 2025-02-25
 */
public interface CatalogRepository extends CrudRepository<CatalogEntity, Long> {
  CatalogEntity findByProductId(String productId);
}