package covy.covyorder.controller;

import covy.covyorder.dto.OrderDto;
import covy.covyorder.entity.OrderEntity;
import covy.covyorder.messagequeue.producer.KafkaProducer;
import covy.covyorder.messagequeue.producer.OrderProducer;
import covy.covyorder.service.OrderService;
import covy.covyorder.vo.RequestOrder;
import covy.covyorder.vo.ResponseOrder;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/order-service")
public class OrderController {

  Environment env;
  OrderService orderService;
  KafkaProducer kafkaProducer;
  OrderProducer orderProducer;

  @Autowired
  public OrderController(Environment env, OrderService orderService, KafkaProducer kafkaProducer, OrderProducer orderProducer) {
    this.env = env;
    this.orderService = orderService;
    this.kafkaProducer = kafkaProducer;
    this.orderProducer = orderProducer;
  }


  @GetMapping("/heath_check")
  public String status() {
    return String.format("It's Working in User Service On PORT %s", env.getProperty("local.server.port"));
  }

  @PostMapping("/{userId}/orders")
  public ResponseEntity<ResponseOrder> createUser(@PathVariable("userId") String userId, @RequestBody RequestOrder order) {
    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    OrderDto orderDto = mapper.map(order, OrderDto.class);
    orderDto.setUserId(userId);
    /* jpa */
//        orderService.createOrder(orderDto);
//        ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);

    /* kafka */
    orderDto.setOrderId(UUID.randomUUID().toString());
    orderDto.setTotalPrice(order.getQty() * order.getUnitPrice());

    /* send this order */
    kafkaProducer.send("example-catalog-topic", orderDto);
//        orderProducer.send("orders", orderDto);

    ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);

    return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
  }

  @GetMapping("/{userId}/orders")
  public ResponseEntity<List<ResponseOrder>> getUser(@PathVariable("userId") String userId) {
    log.info("Before call orders microservice");
    Iterable<OrderEntity> order = orderService.getOrdersByUserId(userId);

    List<ResponseOrder> result = new ArrayList<>();
    order.forEach(v -> {
      result.add(new ModelMapper().map(v, ResponseOrder.class));
    });
    log.info("After call orders microservice");

    return ResponseEntity.status(HttpStatus.OK).body(result);
  }
}