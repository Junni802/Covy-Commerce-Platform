package covy.covyorder.service;

import covy.covyorder.dto.OrderDto;
import covy.covyorder.entity.OrderEntity;

public interface OrderService {
  OrderDto createOrder(OrderDto orderDetails);
  OrderDto getOrderByOrderId(String orderId);
  Iterable<OrderEntity> getOrdersByUserId(String userId);

}