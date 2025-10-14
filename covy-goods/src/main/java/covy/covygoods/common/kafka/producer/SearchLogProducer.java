package covy.covygoods.common.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchLogProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;

  public void sendSearchLog(String userId, String keyword, String category)
      throws JsonProcessingException {
    Map<String, Object> payload = new HashMap<>();
    payload.put("userId", userId);
    payload.put("keyword", keyword);
    payload.put("category", category);
    payload.put("timestamp", System.currentTimeMillis());

    String message = new ObjectMapper().writeValueAsString(payload);
    kafkaTemplate.send("user-search-log", message);
  }
}
