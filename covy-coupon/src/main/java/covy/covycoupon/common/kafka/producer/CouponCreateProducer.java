package covy.covycoupon.common.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponCreateProducer {
  private static final String TOPIC_NAME = "coupon-create";

  private final KafkaTemplate<String, Long> kafkaTemplate;

  public void create(Long userId) {
    // 카프카 템플릿을 사용해서 coupon-create 토픽에 userId를 전송한다. (메시지 발행)
    kafkaTemplate.send(TOPIC_NAME, userId);
    log.info("메시지 발행 성공, topicName : {}, userId : {}", TOPIC_NAME, userId);
  }
}
