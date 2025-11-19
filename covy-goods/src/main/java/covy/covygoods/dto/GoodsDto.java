package covy.covygoods.dto;

import lombok.Data;

@Data
public class GoodsDto {
  private String goodsCd;
  private String goodsNm;
  private String category;
  private Integer price;
  private boolean deleted;  // del_yn → boolean 형태로 매핑
}
