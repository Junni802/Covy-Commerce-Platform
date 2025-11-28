package covy.covycoupon.common.kafka.consumer;

import covy.covycoupon.entity.Coupon;
import covy.covycoupon.model.FailedEvent;
import covy.covycoupon.repository.CouponRepository;
import covy.covycoupon.repository.FailedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CouponCreatedConsumer {
  private final CouponRepository couponRepository;
  private final FailedEventRepository failedEventRepository;

  public CouponCreatedConsumer(CouponRepository couponRepository, FailedEventRepository failedEventRepository) {
    this.couponRepository = couponRepository;
    this.failedEventRepository = failedEventRepository;
  }

  @KafkaListener(
      topics = "coupon-create",    // 토픽 명 지정
      groupId = "group_1",       // 컨슈머 그룹 ID 지정
      containerFactory= "kafkaListenerContainerFactory" // Listner Container Factory 지정
  )
  public void listener(Long userId) {
    try {
      couponRepository.save(new Coupon(userId));
    } catch (Exception e) {
      // 쿠폰 발급에 실패하는 경우
      log.info("failed to create coupon: {}", userId);
      failedEventRepository.save(new FailedEvent(userId));
    }
  }
}