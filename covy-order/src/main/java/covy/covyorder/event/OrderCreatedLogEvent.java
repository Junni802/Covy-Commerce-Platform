package covy.covyorder.event;

public class OrderCreatedLogEvent {

  private String goodsCd;
  private Integer qty;
  private Integer unitPrice;
  private Integer totalPrice;

  private String orderId;
  private String userId;

  @Override
  public String toString() {
    return "OrderCreatedLogEvent ["
        + "goodsCd=" + goodsCd
        + ", qty=" + qty
        + ", unitPrice=" + unitPrice
        + ", totalPrice=" + totalPrice
        + ", orderId=" + orderId
        + ", userId=" + userId
        + "]";
  }
}