package covy.covyorder.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(Include.NON_NULL)
public class ResponseOrder {
  private String goodsCd;
  private Integer qty;
  private Integer unitPrice;
  private Integer totalPrice;
  private Date createAt;

  private String orderId;
}