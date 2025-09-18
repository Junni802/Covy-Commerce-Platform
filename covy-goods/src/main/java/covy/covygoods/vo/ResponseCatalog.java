package covy.covygoods.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ResponseCatalog {

  private String goodsCd;
  private String goodsNm;
  private Integer unitPrice;
  private Integer totalPrice;
  private Integer stock;
}