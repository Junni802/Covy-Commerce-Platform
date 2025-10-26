package covy.covycart.config.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covycart.config.log.ActionType;
import covy.covycart.config.log.UserActionEvent;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatLogProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;

  public void sendCartLogProducer(String userId, String goodsCd, ActionType action)
      throws JsonProcessingException {
    UserActionEvent userActionEvent = UserActionEvent.builder()
        .userId(userId)
        .goodsCd(goodsCd)
        .actionType(action)
        .timestamp(System.currentTimeMillis()).build();

    String message = new ObjectMapper().writeValueAsString(userActionEvent);
    kafkaTemplate.send("cart-event-log", userId, message);
  }

}
