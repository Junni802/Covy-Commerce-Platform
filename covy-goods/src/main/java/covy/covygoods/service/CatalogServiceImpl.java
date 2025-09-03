package covy.covygoods.service;

import covy.covygoods.entity.CatalogEntity;
import covy.covygoods.repository.CatalogRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CatalogServiceImpl implements CatalogService {

  CatalogRepository catalogRepository;

  @Autowired
  public CatalogServiceImpl(CatalogRepository catalogRepository) {
    this.catalogRepository = catalogRepository;
  }

  @Override
  public Iterable<CatalogEntity> getAllCatalogs() {
    return catalogRepository.findAll();
  }
}