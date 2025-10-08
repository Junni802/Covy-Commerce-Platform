package covy.covygoods.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchLogProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;

  public void sendSearchLog(String keyword) {
    kafkaTemplate.send("search-log", keyword);
  }

}
