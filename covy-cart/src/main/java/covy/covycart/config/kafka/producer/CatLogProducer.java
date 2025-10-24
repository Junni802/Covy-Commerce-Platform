package covy.covycart.config.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatLogProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;

  public void sendCartLogProducer(String userId, String goodsCd, String action)
      throws JsonProcessingException {
    Map<String, Object> payload = new HashMap<>();
    payload.put("userId", userId);
    payload.put("goodsCd", goodsCd);
    payload.put("action", action);
    payload.put("timestamp", System.currentTimeMillis());

    String message = new ObjectMapper().writeValueAsString(payload);
    kafkaTemplate.send("cart-event-log" ,message);
  }

}
