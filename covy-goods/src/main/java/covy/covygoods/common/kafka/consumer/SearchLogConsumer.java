package covy.covygoods.common.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@KafkaListener(topics = "search-log", groupId = "search-log-consumer")
public class SearchLogConsumer {

  private final StringRedisTemplate redisTemplate;

  @KafkaHandler
  public void consume(String message) throws JsonProcessingException {
    Map<String, Object> logger = new ObjectMapper().readValue(message, new TypeReference<Map<String, Object>>() {});
    String keyword = (String) logger.get("keyword");

    String redisKey = "popular:keywords";
    redisTemplate.opsForZSet().incrementScore(redisKey, keyword, 1);

    log.info("Search keyword [{}] count incremented in Redis", keyword);
  }

}
