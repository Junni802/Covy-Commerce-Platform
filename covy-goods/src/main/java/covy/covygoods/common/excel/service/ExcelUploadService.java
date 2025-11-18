package covy.covygoods.common.excel.service;

import covy.covygoods.dto.GoodsDto;
import covy.covygoods.entity.GoodsEntity;
import java.io.File;
import java.util.List;

public interface ExcelUploadService {
  List<GoodsEntity> readExcel(File file) throws Exception;
}
