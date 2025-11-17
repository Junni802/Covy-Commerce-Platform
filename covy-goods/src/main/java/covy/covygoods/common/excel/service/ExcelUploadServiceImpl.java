package covy.covygoods.common.excel.service;

import covy.covygoods.dto.GoodsDto;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelUploadServiceImpl implements ExcelUploadService {

  // 컬럼 인덱스 상수
  private static final int COL_GOODS_CD = 0;
  private static final int COL_GOODS_NM = 1;
  private static final int COL_CATEGORY = 2;
  private static final int COL_PRICE = 3;
  private static final int COL_DEL_YN = 4;

  @Override
  public List<GoodsDto> readExcel(File file) throws Exception {
    List<GoodsDto> goodsList = new ArrayList<>();

    try (FileInputStream fis = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(fis)) {

      Sheet sheet = workbook.getSheetAt(0);

      int rowIndex = 0;
      for (Row row : sheet) {

        // 1) 헤더 스킵
        if (rowIndex == 0) {
          rowIndex++;
          continue;
        }

        // 2) 전체 row가 비어있으면 skip
        if (isRowEmpty(row)) {
          rowIndex++;
          continue;
        }

        // 3) DTO 생성
        GoodsDto dto = new GoodsDto();
        dto.setGoodsCd(getCellString(row, COL_GOODS_CD));
        dto.setGoodsNm(getCellString(row, COL_GOODS_NM));
        dto.setCategory(getCellString(row, COL_CATEGORY));
        dto.setPrice(parseSafeInteger(getCellString(row, COL_PRICE)));
        dto.setDeleted(parseYnBoolean(getCellString(row, COL_DEL_YN)));

        // 4) 필수값 누락 시 skip + 로그
        if (dto.getGoodsCd() == null || dto.getGoodsCd().isBlank()
            || dto.getGoodsNm() == null || dto.getGoodsNm().isBlank()
            || dto.getPrice() == null) {
          System.out.println("필수 컬럼 누락 row: " + rowIndex + " → 건너뜀");
          rowIndex++;
          continue;
        }

        // 5) 정상 row 추가
        goodsList.add(dto);
        rowIndex++;
      }
    }

    return goodsList;
  }

  /** 셀 값을 문자열로 안전하게 가져오기 */
  private String getCellString(Row row, int index) {
    Cell cell = row.getCell(index);
    if (cell == null) return null;

    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue().trim();
      case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
      case BOOLEAN -> cell.getBooleanCellValue() ? "Y" : "N";
      case FORMULA -> cell.getCellFormula();
      case BLANK, _NONE, ERROR -> null;
    };
  }

  /** 숫자 문자열을 안전하게 Integer로 변환 */
  private Integer parseSafeInteger(String value) {
    if (value == null || value.isBlank()) return null;

    try {
      value = value.replace(",", "").trim();
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("가격 값이 숫자가 아닙니다: " + value);
    }
  }

  /** "Y"/"N" 문자열을 boolean으로 변환 */
  private boolean parseYnBoolean(String value) {
    return "Y".equalsIgnoreCase(value);
  }

  /** row 전체가 비어있는지 체크 */
  private boolean isRowEmpty(Row row) {
    if (row == null) return true;

    for (int i = 0; i < row.getLastCellNum(); i++) {
      Cell cell = row.getCell(i);
      if (cell != null && cell.getCellType() != CellType.BLANK) {
        return false;
      }
    }
    return true;
  }
}
