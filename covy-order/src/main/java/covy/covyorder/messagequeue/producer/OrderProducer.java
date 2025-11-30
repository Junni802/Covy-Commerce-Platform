package covy.covyorder.messagequeue.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covyorder.dto.Field;
import covy.covyorder.dto.KafkaOrderDto;
import covy.covyorder.dto.OrderDto;
import covy.covyorder.dto.Payload;
import covy.covyorder.dto.Schema;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderProducer {

  private KafkaTemplate kafkaTemplate;

  List<Field> fields = Arrays.asList(new Field("string", true, "order_id"),
      new Field("string", true, "user_id"),
      new Field("string", true, "product_id"),
      new Field("int32", true, "qty"),
      new Field("int32", true, "unit_price"),
      new Field("int32", true, "total_price"));

  Schema schema = Schema.builder()
      .type("struct")
      .fields(fields)
      .optional(false)
      .name("orders")
      .build();

  @Autowired
  public OrderProducer(KafkaTemplate kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public OrderDto send(String topic, OrderDto orderDto) {
    Payload payload = Payload.builder()
        .order_id(orderDto.getOrderId())
        .user_id(orderDto.getUserId())
        .product_id(orderDto.getGoodsCd())
        .qty(orderDto.getQty())
        .unit_price(orderDto.getUnitPrice())
        .total_price(orderDto.getTotalPrice())
        .build();

    KafkaOrderDto kafkaOrderDto = new KafkaOrderDto(schema, payload);
    ObjectMapper mapper = new ObjectMapper();
    String jsonInString = "";
    try {
      jsonInString = mapper.writeValueAsString(kafkaOrderDto);
    } catch (JsonProcessingException ex) {
      ex.printStackTrace();
    }

    kafkaTemplate.send(topic, jsonInString);
    log.info("Order Producer sent data from the Order microservice : " + orderDto);

    return orderDto;
  }
}