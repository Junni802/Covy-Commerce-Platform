package covy.covygoods.common.excel.service;

import covy.covygoods.dto.GoodsDto;
import java.io.File;
import java.util.List;

public interface ExcelUploadService {
  List<GoodsDto> readExcel(File file) throws Exception;
}
