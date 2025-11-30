package covy.covyorder.vo;

import lombok.Data;

@Data
public class RequestOrder {

  private String goodsCd;

  private Integer qty;

  private Integer unitPrice;

}