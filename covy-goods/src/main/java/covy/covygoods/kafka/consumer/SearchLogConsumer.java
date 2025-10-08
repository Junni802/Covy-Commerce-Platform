package covy.covygoods.kafka.consumer;

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
  public void consume(String keyword) {
    String redisKey = "popular:keywords";

    // 검색어의 점수 증가 (오름차순 정렬을 위한 Zset)
    redisTemplate.opsForZSet().incrementScore(redisKey, keyword, 1);

    log.info("Search keyword [{}] count incremented in Redis", keyword);
  }

}
