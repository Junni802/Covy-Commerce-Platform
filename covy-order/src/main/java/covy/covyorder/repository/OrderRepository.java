package covy.covyorder.repository;

import covy.covyorder.entity.OrderEntity;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<OrderEntity, Long> {
  OrderEntity findByOrderId(String orderId);
  Iterable<OrderEntity> findByUserId(String userId);
}