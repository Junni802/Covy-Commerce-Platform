package covy.covyorder.dto;

import lombok.Data;

@Data
public class OrderDto {

  private String goodsCd;
  private Integer qty;
  private Integer unitPrice;
  private Integer totalPrice;

  private String orderId;
  private String userId;

}