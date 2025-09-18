package covy.covygoods.dto;

import lombok.Data;

@Data
public class GoodsDto {

  private String goodsCd;
  private Integer qty;
  private Integer unitPrice;
  private Integer totalPrice;

  private String orderId;
  private String userId;

}