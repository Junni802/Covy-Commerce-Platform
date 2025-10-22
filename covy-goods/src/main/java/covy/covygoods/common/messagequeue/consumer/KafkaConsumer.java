package covy.covygoods.common.messagequeue.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covygoods.entity.GoodsEntity;
import covy.covygoods.repository.GoodsRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumer {
  GoodsRepository repository;

  @Autowired
  public KafkaConsumer(GoodsRepository repository) {
    this.repository = repository;
  }

  @KafkaListener(topics = "example-catalog-topic")
  public void updateQty(String kafkaMessage) {
    log.info("Kafka Message: ->" + kafkaMessage);

    Map<Object, Object> map = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    try {
      map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
    }catch (JsonProcessingException ex) {
      ex.printStackTrace();
    }

    Optional<GoodsEntity> entity = repository.findByGoodsCd((String)map.get("goodsCd"));
    if (!entity.isPresent()) {
      entity.get().setStock(entity.get().getStock() - (Integer) map.get("qty"));
      repository.save(entity.get());
    }

  }


}